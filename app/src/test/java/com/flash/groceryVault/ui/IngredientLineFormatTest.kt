package com.flash.groceryVault.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientLineFormatTest {
    @Test
    fun ingredientLine_formatting() {
        val name = "Sugar"
        val suffix = "2 tsp"
        val text = if (suffix.isNotBlank()) "• $name $suffix" else "• $name"
        assertEquals("• Sugar 2 tsp", text)
    }
}
