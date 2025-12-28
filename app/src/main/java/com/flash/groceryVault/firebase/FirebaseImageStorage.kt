package com.flash.groceryVault.firebase

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

open class FirebaseImageStorage(
    private val context: Context,
    private val auth: FirebaseAuth? = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private fun uid(): String = auth?.currentUser?.uid ?: error("User not logged in")

    /**
     * Uploads image to: users/<uid>/grocerys/<groceryId>/<random>.jpg
     * Returns public downloadUrl (String) to store in Firestore/Room.
     */
    suspend fun uploadGroceryImage(groceryId: Long, localUri: String): String {
        val bitmap = context.contentResolver.openInputStream(localUri.toUri()).use { input ->
            BitmapFactory.decodeStream(input)
        } ?: error("Failed to decode image")

        // Resize (max 1080px) to control memory & file size
        val resized = bitmap.scaleDown(maxSize = 1080)

        // Compress to JPEG (~70% quality → ~100–200 KB typical)
        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val bytes = baos.toByteArray()

        val ref = storage.reference
            .child("users")
            .child(uid())
            .child("grocerys")
            .child(groceryId.toString())
            .child("${UUID.randomUUID()}.jpg")

        // Upload compressed bytes
        ref.putBytes(bytes).await()

        return ref.downloadUrl.await().toString()
    }

    /** Optional: delete a previously uploaded image by URL */
    suspend fun deleteByUrl(downloadUrl: String) {
        storage.getReferenceFromUrl(downloadUrl).delete().await()
    }
}

private fun Bitmap.scaleDown(maxSize: Int): Bitmap {
    val ratio = width.toFloat() / height.toFloat()
    val (targetW, targetH) = if (ratio > 1) {
        maxSize to (maxSize / ratio).toInt()
    } else {
        (maxSize * ratio).toInt() to maxSize
    }
    return this.scale(targetW, targetH)
}