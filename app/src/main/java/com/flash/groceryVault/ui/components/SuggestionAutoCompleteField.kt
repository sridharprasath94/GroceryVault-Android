package com.flash.groceryVault.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.PopupProperties

enum class MatchMode { Prefix, Contains }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionAutoCompleteField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    showDropdownIcon: Boolean = true,
    matchMode: MatchMode = MatchMode.Prefix,
    maxItems: Int = 8,
) {
    var expanded by remember { mutableStateOf(false) }

    val query = value.text

    val filtered = remember(query, suggestions, matchMode) {
        if (suggestions.isEmpty()) emptyList()
        else if (query.isBlank()) suggestions.take(maxItems)
        else {
            val q = query.lowercase()
            val seq = suggestions.asSequence()
            val matched = when (matchMode) {
                MatchMode.Prefix -> seq.filter { it.lowercase().startsWith(q) }
                MatchMode.Contains -> seq.filter { it.contains(query, ignoreCase = true) }
            }
            matched.take(maxItems).toList()
        }
    }

    val showMenu = expanded && filtered.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            trailingIcon = {
                if (showDropdownIcon) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
        ) {
            filtered.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(TextFieldValue(item, selection = TextRange(item.length)))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SuggestionAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    showDropdownIcon: Boolean = true,
    matchMode: MatchMode = MatchMode.Prefix,
    maxItems: Int = 8,
) {
    var tfv by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    // Keep internal state in sync if parent updates `value` from outside (e.g. loading/editing)
    LaunchedEffect(value) {
        if (value != tfv.text) {
            tfv = TextFieldValue(value, selection = TextRange(value.length))
        }
    }
    SuggestionAutoCompleteField(
        value = tfv,
        onValueChange = { newTfv ->
            tfv = newTfv
            onValueChange(newTfv.text)
        },
        suggestions = suggestions,
        label = label,
        modifier = modifier,
        showDropdownIcon = showDropdownIcon,
        matchMode = matchMode,
        maxItems = maxItems,
    )
}
