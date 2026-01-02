package com.flash.groceryVault.util

import com.flash.groceryVault.ui.util.SimpleJson
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleJsonTest {

    @Test
    fun encode_list_and_escape() {
        val json = SimpleJson.encode(listOf("a", "b\n"))
        assertEquals("[\"a\",\"b\\n\"]", json)
    }
}
