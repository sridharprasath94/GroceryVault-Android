package com.flash.groceryVault.data

import com.flash.groceryVault.ui.util.SimpleJson
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class GroceryRepository(
    private val dao: GroceryDao,
) {

    fun observeLists(): Flow<List<GroceryListEntity>> = dao.observeLists()

    fun observeListWithItems(id: Long): Flow<GroceryListWithItems?> = dao.observeListWithItems(id)

    suspend fun createList(
        title: String,
        description: String?,
        items: List<String>,
    ): Long {
        val now = System.currentTimeMillis()
        val listId = dao.insertList(
            GroceryListEntity(
                title = title,
                description = description?.trim()?.ifEmpty { null },
                createdAt = now,
                updatedAt = now,
            )
        )

        val cleanItems = items
            .map { it.trim() }
            .filter { it.isNotBlank() }

        dao.insertItems(
            cleanItems.mapIndexed { idx, name ->
                GroceryItemEntity(
                    listId = listId,
                    name = name,
                    isChecked = false,
                    sortOrder = idx,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        )
        return listId
    }

    suspend fun updateList(
        id: Long,
        title: String,
        description: String?,
        items: List<Pair<String, Boolean>>,
    ) {
        val now = System.currentTimeMillis()
        dao.updateList(id, title = title, description = description?.trim()?.ifEmpty { null }, updatedAt = now)

        // Replace items for simplicity (stable & predictable for production with small lists)
        dao.deleteItemsForList(id)

        val clean = items
            .map { (n, c) -> n.trim() to c }
            .filter { it.first.isNotBlank() }

        dao.insertItems(
            clean.mapIndexed { idx, (name, checked) ->
                GroceryItemEntity(
                    listId = id,
                    name = name,
                    isChecked = checked,
                    sortOrder = idx,
                    createdAt = now,
                    updatedAt = now
                )
            }
        )
    }

    suspend fun setItemChecked(itemId: Long, checked: Boolean) {
        dao.setItemChecked(itemId, checked, updatedAt = System.currentTimeMillis())
    }

    suspend fun deleteList(listId: Long) {
        val now = System.currentTimeMillis()
        dao.markListDeleted(listId, deletedAt = now, updatedAt = now)
    }

    suspend fun deleteAll() = dao.clearAll()

    // ---- Sync helpers ----

    suspend fun getAllLocalListsIncludingDeleted(): List<GroceryListEntity> =
        dao.getAllListsIncludingDeletedOnce()

    suspend fun getListWithItemsOnce(listId: Long): GroceryListWithItems? {
        val list = dao.getListOnce(listId) ?: return null
        val items = dao.getItemsOnce(listId)
        return GroceryListWithItems(list = list, items = items)
    }

    suspend fun applyRemoteList(remote: GroceryListEntity, remoteItems: List<GroceryItemEntity>) {
        // Upsert list
        dao.upsertLists(listOf(remote))

        // Items: replace list items
        dao.deleteItemsForList(remote.id)
        if (!remote.isDeleted) {
            dao.insertItems(
                remoteItems.mapIndexed { idx, it ->
                    it.copy(
                        id = 0, // local row id; we don't preserve per-item ids in this simple sync model
                        listId = remote.id,
                        sortOrder = idx
                    )
                }
            )
        }
    }

    // ---- Backup JSON ----

    suspend fun exportAllAsJson(): String {
        val lists = getAllLocalListsIncludingDeleted()
        val payload = lists.map { list ->
            val items = dao.getItemsOnce(list.id).map { i ->
                mapOf(
                    "name" to i.name,
                    "isChecked" to i.isChecked,
                    "sortOrder" to i.sortOrder,
                )
            }
            mapOf(
                "id" to list.id,
                "title" to list.title,
                "description" to list.description,
                "isDeleted" to list.isDeleted,
                "deletedAt" to list.deletedAt,
                "createdAt" to list.createdAt,
                "updatedAt" to list.updatedAt,
                "items" to items,
            )
        }
        return SimpleJson.encode(payload)
    }

    suspend fun importFromJson(json: String) {
        val arr = JSONArray(json)
        val lists = mutableListOf<GroceryListEntity>()
        val itemsByList = mutableMapOf<Long, List<GroceryItemEntity>>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getLong("id")
            val list = GroceryListEntity(
                id = id,
                title = o.getString("title"),
                description = o.optString("description").takeIf { it.isNotBlank() },
                isDeleted = o.optBoolean("isDeleted", false),
                deletedAt = if (o.isNull("deletedAt")) null else o.getLong("deletedAt"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
            )
            lists.add(list)

            val itemsJson = o.optJSONArray("items") ?: JSONArray()
            val items = mutableListOf<GroceryItemEntity>()
            for (j in 0 until itemsJson.length()) {
                val it = itemsJson.getJSONObject(j)
                items.add(
                    GroceryItemEntity(
                        id = 0,
                        listId = id,
                        name = it.getString("name"),
                        isChecked = it.optBoolean("isChecked", false),
                        sortOrder = it.optInt("sortOrder", j),
                        createdAt = list.createdAt,
                        updatedAt = list.updatedAt,
                    )
                )
            }
            itemsByList[id] = items
        }

        dao.upsertLists(lists)
        // Replace all items
        dao.clearItems()
        val allItems = itemsByList.values.flatten()
        if (allItems.isNotEmpty()) dao.insertItems(allItems)
    }
}
