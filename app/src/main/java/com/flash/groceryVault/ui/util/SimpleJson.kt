package com.flash.groceryVault.ui.util

import kotlin.collections.iterator
import kotlin.text.iterator

object SimpleJson {
    fun encode(value: Any?): String = buildString { appendValue(value) }

    private fun StringBuilder.appendValue(v: Any?) {
        when (v) {
            null -> append("null")
            is String -> append('"').append(escape(v)).append('"')
            is Number, is Boolean -> append(v.toString())
            is Map<*, *> -> {
                append("{")
                var first = true
                for ((k, value) in v) {
                    if (k !is String) continue
                    if (!first) append(",") else first = false
                    append('"').append(escape(k)).append('"').append(":")
                    appendValue(value)
                }
                append("}")
            }
            is Iterable<*> -> {
                append("[")
                var first = true
                for (item in v) {
                    if (!first) append(",") else first = false
                    appendValue(item)
                }
                append("]")
            }
            else -> append('"').append(escape(v.toString())).append('"')
        }
    }

    private fun escape(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}
