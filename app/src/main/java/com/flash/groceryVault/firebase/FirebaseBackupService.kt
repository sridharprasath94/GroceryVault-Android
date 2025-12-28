package com.flash.groceryVault.firebase

import com.flash.groceryVault.data.GroceryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseBackupService(
    private val repo: GroceryRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun uid(): String = auth.currentUser?.uid ?: error("User not logged in")

    private fun doc() = firestore.collection("users").document(uid()).collection("backup").document("latest")

    suspend fun backupNow() {
        val json = repo.exportAllAsJson()
        doc().set(
            mapOf(
                "json" to json,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun restoreLatest() {
        val snap = doc().get().await()
        val json = snap.getString("json") ?: return
        repo.importFromJson(json)
    }
}
