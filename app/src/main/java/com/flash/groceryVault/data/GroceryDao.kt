package com.flash.groceryVault.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_lists WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun observeLists(): Flow<List<GroceryListEntity>>

    @Transaction
    @Query("SELECT * FROM grocery_lists WHERE id = :id LIMIT 1")
    fun observeListWithItems(id: Long): Flow<GroceryListWithItems?>

    @Insert
    suspend fun insertList(list: GroceryListEntity): Long

    @Query("UPDATE grocery_lists SET title = :title, description = :description, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateList(id: Long, title: String, description: String?, updatedAt: Long)

    @Query("UPDATE grocery_lists SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markListDeleted(id: Long, deletedAt: Long, updatedAt: Long)

    @Query("SELECT * FROM grocery_lists WHERE id = :id LIMIT 1")
    suspend fun getListOnce(id: Long): GroceryListEntity?

    @Query("SELECT * FROM grocery_lists ORDER BY createdAt DESC")
    suspend fun getAllListsIncludingDeletedOnce(): List<GroceryListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLists(lists: List<GroceryListEntity>)

    // Items
    @Insert
    suspend fun insertItems(items: List<GroceryItemEntity>)

    @Query("DELETE FROM grocery_items WHERE listId = :listId")
    suspend fun deleteItemsForList(listId: Long)

    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY sortOrder ASC")
    suspend fun getItemsOnce(listId: Long): List<GroceryItemEntity>

    @Query("UPDATE grocery_items SET isChecked = :checked, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setItemChecked(id: Long, checked: Boolean, updatedAt: Long)

    // Clear
    @Query("DELETE FROM grocery_items") suspend fun clearItems()
    @Query("DELETE FROM grocery_lists") suspend fun clearLists()

    @Transaction
    suspend fun clearAll() {
        clearItems()
        clearLists()
    }
}
