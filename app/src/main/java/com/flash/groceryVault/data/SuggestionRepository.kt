package com.flash.groceryVault.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.insert

class SuggestionsRepository(
    private val dao: SuggestionDao,
) {
    /** Observe merged defaults + user-added entries (stored in Room) */
    fun observeAllMerged(type: SuggestionType): Flow<List<String>> {
        return dao.observeAll(type).map { dbList ->
            (dbList.map { it.value })
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sorted()
                .toList()
        }
    }

    suspend fun searchPrefix(type: SuggestionType, prefix: String, limit: Int = 20): List<String> =
        dao.searchPrefix(type, prefix.lowercase(), limit).map { it.value }

    /** Persist a user-added suggestion (IGNORE duplicates). */
    suspend fun add(type: SuggestionType, value: String) {
        val clean = value.trim()
        if (clean.isBlank()) return

        val lower = clean.lowercase()
        Log.d("SuggestionsRepository", "Adding suggestion: type=$type, value='$clean'")
        dao.insert(
            SuggestionEntity(
                key = "${type.name}:$lower",
                type = type,
                value = clean,
                valueLower = lower,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun seedDefaultsIfEmpty(type: SuggestionType, defaults: List<String>) {
        val existing = dao.searchPrefix(type, prefix = "", limit = 1)
        if (existing.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val items = defaults
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .map { v ->
                val lower = v.lowercase()
                SuggestionEntity(
                    key = "${type.name}:$lower",
                    type = type,
                    value = v,
                    valueLower = lower,
                    createdAt = now
                )
            }
        dao.insertAll(items)
    }

    suspend fun addMany(type: SuggestionType, values: Iterable<String?>) {
        values
            .asSequence()
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { add(type, it) }
    }

}
