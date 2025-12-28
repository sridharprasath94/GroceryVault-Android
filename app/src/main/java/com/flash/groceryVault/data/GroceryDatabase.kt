package com.flash.groceryVault.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GroceryListEntity::class,
        GroceryItemEntity::class,
        SuggestionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GroceryDatabase : RoomDatabase() {

    abstract fun groceryDao(): GroceryDao
    abstract fun suggestionDao(): SuggestionDao

    companion object {
        @Volatile private var Instance: GroceryDatabase? = null

        fun getDatabase(context: Context): GroceryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GroceryDatabase::class.java,
                    "grocery_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
