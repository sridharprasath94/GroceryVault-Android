package com.flash.groceryVault.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SuggestionsRepository(
    private val dao: SuggestionDao,
) {
    /** Observe merged defaults + user-added entries (stored in Room) */
    fun observeAllMerged(type: SuggestionType): Flow<List<String>> =
        dao.observeAll(type).map { it.map(SuggestionEntity::value) }

    suspend fun searchPrefix(type: SuggestionType, prefix: String, limit: Int = 20): List<String> =
        dao.searchPrefix(type, prefix.lowercase(), limit).map { it.value }

    suspend fun add(type: SuggestionType, value: String) {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return

        val lower = trimmed.lowercase()
        val entity = SuggestionEntity(
            key = "${type.name}:$lower",
            type = type,
            value = trimmed,
            valueLower = lower,
            createdAt = System.currentTimeMillis(),
        )
        dao.insert(entity)
    }

    suspend fun addMany(type: SuggestionType, values: Iterable<String?>) {
        values
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { add(type, it) }
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
}
