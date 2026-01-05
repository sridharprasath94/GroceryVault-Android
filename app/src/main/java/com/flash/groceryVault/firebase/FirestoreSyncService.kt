package com.flash.groceryVault.firebase

import com.flash.groceryVault.data.GroceryItemEntity
import com.flash.groceryVault.data.GroceryListEntity
import com.flash.groceryVault.data.GroceryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Two-way sync: per-list documents under users/<uid>/lists/<listId>.
 * Conflict: updatedAt (last-write-wins). Tombstones supported via isDeleted/deletedAt.
 *
 * NOTE: This keeps item rows embedded as an array to keep the model simple.
 * For huge lists, switch to per-item documents.
 */
class FirestoreSyncService(
    private val repo: GroceryRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    private fun uid(): String = auth.currentUser?.uid ?: error("User not logged in")

    private fun col() = firestore.collection("users").document(uid()).collection("lists")

    private var listener: ListenerRegistration? = null

    suspend fun syncNow() {
        val locals = repo.getAllLocalListsIncludingDeleted()

        // 1) Push local -> remote
        for (local in locals) {
            val details = repo.getListWithItemsOnce(local.id)
            val data = mutableMapOf<String, Any?>(
                "id" to local.id,
                "title" to local.title,
                "description" to local.description,
                "isDeleted" to local.isDeleted,
                "deletedAt" to local.deletedAt,
                "createdAt" to local.createdAt,
                "updatedAt" to local.updatedAt,
            )

            val items = details?.items?.sortedBy { it.sortOrder }?.map {
                mapOf(
                    "name" to it.name,
                    "isChecked" to it.isChecked,
                    "sortOrder" to it.sortOrder,
                )
            } ?: emptyList<Map<String, Any?>>()

            data["items"] = items

            col().document(local.id.toString()).set(data).await()
        }

        // 2) Pull remote -> local
        val snap = col().get().await()
        for (doc in snap.documents) {
            val o = doc.data ?: continue
            val id = (o["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
            val remote = GroceryListEntity(
                id = id,
                title = o["title"] as? String ?: "",
                description = o["description"] as? String,
                isDeleted = o["isDeleted"] as? Boolean ?: false,
                deletedAt = (o["deletedAt"] as? Number)?.toLong(),
                createdAt = (o["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (o["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            )

            val local = repo.getAllLocalListsIncludingDeleted().firstOrNull { it.id == id }
            val shouldApply = local == null || remote.updatedAt > local.updatedAt

            if (shouldApply) {
                val itemsArr = o["items"] as? List<Map<String, Any?>>
                val items = (itemsArr ?: emptyList()).mapIndexed { idx, it ->
                    GroceryItemEntity(
                        id = 0,
                        listId = id,
                        name = it["name"] as? String ?: "",
                        isChecked = it["isChecked"] as? Boolean ?: false,
                        sortOrder = (it["sortOrder"] as? Number)?.toInt() ?: idx,
                        createdAt = remote.createdAt,
                        updatedAt = remote.updatedAt,
                    )
                }
                repo.applyRemoteList(remote, items)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startRealTime() {
        if (listener != null) return

        listener = col().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener

            // Apply each changed doc to local (best-effort)
            for (doc in snap.documents) {
                val o = doc.data ?: continue
                val id = (o["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue

                val remote = GroceryListEntity(
                    id = id,
                    title = o["title"] as? String ?: "",
                    description = o["description"] as? String,
                    isDeleted = o["isDeleted"] as? Boolean ?: false,
                    deletedAt = (o["deletedAt"] as? Number)?.toLong(),
                    createdAt = (o["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (o["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                )

                val itemsArr = o["items"] as? List<Map<String, Any?>>
                val items = (itemsArr ?: emptyList()).mapIndexed { idx, it ->
                    GroceryItemEntity(
                        id = 0,
                        listId = id,
                        name = it["name"] as? String ?: "",
                        isChecked = it["isChecked"] as? Boolean ?: false,
                        sortOrder = (it["sortOrder"] as? Number)?.toInt() ?: idx,
                        createdAt = remote.createdAt,
                        updatedAt = remote.updatedAt,
                    )
                }

                // Run on background using a coroutine (fire-and-forget)
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching { repo.applyRemoteList(remote, items) }
                }
            }
        }
    }

    fun stopRealTime() {
        listener?.remove()
        listener = null
    }
}
