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
        @Volatile
        private var Instance: GroceryDatabase? = null

        @Volatile
        private var LAST_NAME: String? = null

        /**
         * Default DB (fallback). In this app we use per-user databases named recipe_db_<uid>.
         */
        fun getDatabase(context: Context): GroceryDatabase = getDatabase(context, "grocery_db")
        fun getDatabase(context: Context, dbName: String): GroceryDatabase {
            val existing = Instance
            if (existing != null && LAST_NAME == dbName) return existing
            return synchronized(this) {
                val current = Instance
                if (current != null && LAST_NAME == dbName) return@synchronized current
                Room.databaseBuilder(
                    context.applicationContext,
                    GroceryDatabase::class.java,
                    dbName
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also {
                        Instance = it
                        LAST_NAME = dbName

                    }
            }
        }
    }
}
