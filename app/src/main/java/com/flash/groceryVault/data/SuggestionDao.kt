package com.flash.groceryVault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {

    @Query(
        """
        SELECT * FROM suggestions
        WHERE type = :type AND valueLower LIKE :prefix || '%'
        ORDER BY valueLower
        LIMIT :limit
        """
    )
    suspend fun searchPrefix(type: SuggestionType, prefix: String, limit: Int = 20): List<SuggestionEntity>

    @Query(
        """
        SELECT * FROM suggestions
        WHERE type = :type
        ORDER BY valueLower
        """
    )
    fun observeAll(type: SuggestionType): Flow<List<SuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SuggestionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<SuggestionEntity>)
}
