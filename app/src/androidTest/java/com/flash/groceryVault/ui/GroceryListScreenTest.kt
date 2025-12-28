package com.flash.groceryVault.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flash.groceryVault.data.GroceryDatabase
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.firebase.FirebaseImageStorage
import com.flash.groceryVault.ui.theme.GroceryVaultTheme
import org.junit.Rule
import org.junit.Test

class FakeFirebaseImageStorage : FirebaseImageStorage(
    context = ApplicationProvider.getApplicationContext(),
    auth = null
) {
    // Override methods if needed for testing
}

class GroceryListScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyState() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, GroceryDatabase::class.java)
            .allowMainThreadQueries().build()
        val repo = GroceryRepository(db.groceryDao(), imageStorage = FakeFirebaseImageStorage())

        rule.setContent {
            GroceryVaultTheme {
                GroceryListScreenForTest(repo = repo)
            }
        }

        rule.onNodeWithText("No grocerys yet. Tap + to add one.").assertIsDisplayed()
        db.close()
    }
}
