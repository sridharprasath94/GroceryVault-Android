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

    private val db by lazy { GroceryDatabase.getDatabase(appContext) }

    val groceryRepository: GroceryRepository by lazy {
        GroceryRepository(dao = db.groceryDao())
    }

    val suggestionsRepository: SuggestionsRepository by lazy {
        SuggestionsRepository(dao = db.suggestionDao())
    }

    fun firestoreSyncServiceForCurrentUser(): FirestoreSyncService {
        return FirestoreSyncService(
            repo = groceryRepository,
            auth = auth,
            firestore = firestore
        )
    }

    fun firebaseBackupServiceForCurrentUser(): FirebaseBackupService {
        return FirebaseBackupService(
            repo = groceryRepository,
            auth = auth,
            firestore = firestore
        )
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
