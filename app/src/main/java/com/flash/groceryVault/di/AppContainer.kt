package com.flash.groceryVault.di

import android.content.Context
import com.flash.groceryVault.data.GroceryDatabase
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.data.SuggestionType
import com.flash.groceryVault.data.SuggestionsRepository
import com.flash.groceryVault.data.defaults.DefaultSuggestionsProvider
import com.flash.groceryVault.firebase.FirebaseBackupService
import com.flash.groceryVault.firebase.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * AppContainer = lightweight DI / Service Locator.
 *
 * Why it exists:
 * - Creates singletons (Room DB, repositories, sync services) in ONE place.
 * - Keeps Composables + ViewModels free from `new ...()` chains.
 * - Makes swapping fakes easier for Previews / tests.
 * - Avoids accidentally creating multiple DB instances.
 */
class AppContainer(
    private val appContext: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    // Cache per-user instances to avoid recreating them on every call.
    private var cachedUid: String? = null
    private var cachedDb: GroceryDatabase? = null
    private var cachedGroceryRepo: GroceryRepository? = null
    private var cachedSuggestionsRepo: SuggestionsRepository? = null
    private var cachedSync: FirestoreSyncService? = null
    private var cachedBackup: FirebaseBackupService? = null

    /**
     * Ensures caches match the current user. If user changed, rebuild user-scoped objects.
     */
    private fun ensureUserCache(): String {
        val uid = auth.currentUser?.uid ?: error("User not logged in")
        if (cachedUid != uid) {
            clearUserScopedCaches()
            cachedUid = uid
        }
        return uid
    }

    /**
     * Clears all instances that are scoped to the currently logged in user.
     * Call this on logout or whenever user changes.
     */
    private fun clearUserScopedCaches() {
        cachedUid = null
        cachedDb = null
        cachedGroceryRepo = null
        cachedSuggestionsRepo = null
        cachedSync = null
        cachedBackup = null
    }


    val groceryRepositoryForCurrentUser: GroceryRepository by lazy {
        val uid = ensureUserCache()

        val db = cachedDb ?: GroceryDatabase.getDatabase(
            appContext,
            dbName = "grocery_db_$uid"
        ).also { cachedDb = it }

        return@lazy cachedGroceryRepo ?: GroceryRepository(dao = db.groceryDao()).also {
            cachedGroceryRepo = it
        }
    }

    val suggestionsRepository: SuggestionsRepository by lazy {
        val uid = ensureUserCache()
        val db = cachedDb ?: GroceryDatabase.getDatabase(
            appContext,
            dbName = "grocery_db_$uid"
        ).also { cachedDb = it }
        return@lazy cachedSuggestionsRepo ?: SuggestionsRepository(dao = db.suggestionDao()).also {
            cachedSuggestionsRepo = it
        }
    }

    fun firestoreSyncServiceForCurrentUser(): FirestoreSyncService {
        ensureUserCache()
        return cachedSync ?: FirestoreSyncService(
            repo = groceryRepositoryForCurrentUser,
            auth = auth,
            firestore = firestore
        ).also {
            cachedSync = it
        }
    }

    fun firebaseBackupServiceForCurrentUser(): FirebaseBackupService {
        ensureUserCache()
        return FirebaseBackupService(
            repo = groceryRepositoryForCurrentUser,
            auth = auth,
            firestore = firestore
        ).also {
            cachedBackup = it
        }
    }

    fun signOut() {
        // currently only FirebaseAuth; Google client sign-out is handled in UI
        auth.signOut()
    }

    suspend fun seedSuggestionDefaultsIfEmpty() {
        suggestionsRepository.seedDefaultsIfEmpty(
            type = SuggestionType.GROCERY_ITEM,
            defaults = DefaultSuggestionsProvider.grocerySuggestions(appContext)
        )
        suggestionsRepository.seedDefaultsIfEmpty(
            type = SuggestionType.STEP,
            defaults = DefaultSuggestionsProvider.steps(appContext)
        )
    }
}
