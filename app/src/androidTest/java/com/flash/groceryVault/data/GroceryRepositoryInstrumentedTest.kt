package com.flash.groceryVault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroceryRepositoryInstrumentedTest {

    private lateinit var db: GroceryDatabase
    private lateinit var repo: GroceryRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, GroceryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = GroceryRepository(db.groceryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createGrocery_thenAppearsInList() = runBlocking {
        val id = repo.createGrocery(
            title = "Pasta",
            description = "Quick dinner",
            imageUri = null,
            imageUrl = null,
            ingredients = listOf(Triple("Noodles", "200", "g")),
            steps = listOf("Boil water", "Cook noodles")
        )

        val list = repo.observeGrocerys().first()
        assertEquals(1, list.size)
        assertEquals(id, list.first().id)
        assertEquals("Pasta", list.first().title)
    }
}
