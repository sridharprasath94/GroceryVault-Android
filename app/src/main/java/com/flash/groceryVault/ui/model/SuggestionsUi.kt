package com.flash.groceryVault.ui.model


data class SuggestionsUi(
    val ingredients: List<String> = emptyList(),
    val units: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
)