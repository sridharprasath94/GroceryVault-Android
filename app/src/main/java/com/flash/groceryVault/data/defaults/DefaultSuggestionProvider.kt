package com.flash.groceryVault.data.defaults

import android.content.Context
import androidx.annotation.RawRes
import com.flash.groceryVault.R

object DefaultSuggestionsProvider {

    fun grocerySuggestions(context: Context): List<String> =
        readLines(context, R.raw.grocery_suggestions)

    fun steps(context: Context): List<String> =
        readLines(context, R.raw.default_steps)

    private fun readLines(context: Context, @RawRes resId: Int): List<String> {
        return context.resources
            .openRawResource(resId)
            .bufferedReader()
            .useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
    }
}
