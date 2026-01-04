package com.flash.groceryVault.data

import android.util.Log
import com.flash.groceryVault.ui.util.SimpleJson
import com.flash.groceryVault.ui.util.toFormattedDateTimeLegacy
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.lang.String.format

class GroceryRepository(
    private val dao: GroceryDao,
) {

    fun observeLists(): Flow<List<GroceryListEntity>> = dao.observeLists()

    fun observeListsWithItems(): Flow<List<GroceryListWithItems>> =
        dao.observeListsWithItems()

    fun observeListWithItems(id: Long): Flow<GroceryListWithItems?> = dao.observeListWithItems(id)

    suspend fun createList(
        title: String,
        description: String?,
        items: List<Pair<String, Boolean>>,
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
            .map { it.first.trim() to it.second }
            .filter { it.first.isNotBlank() }

        dao.insertItems(
            cleanItems.mapIndexed { idx, item ->
                GroceryItemEntity(
                    listId = listId,
                    name = item.first,
                    isChecked = item.second,
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
        dao.updateList(
            id,
            title = title,
            description = description?.trim()?.ifEmpty { null },
            updatedAt = now
        )

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
        val now = System.currentTimeMillis()

        // 1️⃣ Update item
        dao.setItemChecked(
            id = itemId,
            checked = checked,
            updatedAt = now
        )

        // 2️⃣ Find parent list
        val listId = dao.getListIdForItem(itemId) ?: return

        Log.d(
            "GroceryRepository", "setItemChecked: item $itemId in list $listId set to" +
                    " $checked. Updated at in 24hour format is ${
                        now.toFormattedDateTimeLegacy()
                    }"
        )

        // 3️⃣ Touch parent list updatedAt
        dao.updateListUpdatedAt(
            listId = listId,
            updatedAt = now
        )
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

    suspend fun applyRemoteList(
        remote: GroceryListEntity,
        remoteItems: List<GroceryItemEntity>
    ) {
        val local = dao.getListOnce(remote.id)
        Log.d(
            "GroceryRepository", format(
                "applyRemoteList: remote list %d (updatedAt=%s), local=%s",
                remote.id,
                remote.updatedAt.toFormattedDateTimeLegacy(),
                local?.updatedAt?.toFormattedDateTimeLegacy() ?: "null"
            )
        )
        if (local != null && remote.updatedAt < local.updatedAt) {
            // Local is newer; do not apply remote.
            return
        }
        if (remote.isDeleted) {
            dao.markListDeleted(
                remote.id,
                deletedAt = remote.deletedAt ?: System.currentTimeMillis(),
                updatedAt = remote.updatedAt
            )
            dao.deleteItemsForList(remote.id)
            return
        }
        dao.upsertLists(listOf(remote))
        dao.deleteItemsForList(remote.id)
        dao.insertItems(
            remoteItems.mapIndexed { idx, it ->
                it.copy(
                    id = 0,
                    listId = remote.id,
                    sortOrder = idx
                )
            }
        )
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
