import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionAutoCompleteField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    maxResults: Int = 8,
    showDropdownIcon: Boolean = true,   // ✅ optional, default ON
    matchMode: MatchMode = MatchMode.Contains,
) {
    var expanded by remember { mutableStateOf(false) }

    val query = value.text
    val filtered = remember(query, suggestions, matchMode) {
        if (suggestions.isEmpty()) emptyList()
        else if (query.isBlank()) suggestions.take(maxResults)
        else suggestions
            .asSequence()
            .filter {
                when (matchMode) {
                    MatchMode.Contains -> it.contains(query, ignoreCase = true)
                    MatchMode.Prefix -> it.startsWith(query, ignoreCase = true)
                }
            }
            .take(maxResults)
            .toList()
    }

    val showMenu = expanded && filtered.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                    !expanded
                ),
            trailingIcon = {
                if (showDropdownIcon) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )
        ExposedDropdownMenu (
            expanded = showMenu,
            onDismissRequest = { expanded = false },
            matchAnchorWidth = true,
        ) {
            filtered.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(
                            TextFieldValue(
                                text = item,
                                selection = TextRange(item.length)
                            )
                        )
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
    maxResults: Int = 8,
    showDropdownIcon: Boolean = true,
    matchMode: MatchMode = MatchMode.Contains,
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
        maxResults = maxResults,
        showDropdownIcon = showDropdownIcon,
        matchMode = matchMode
    )
}

enum class MatchMode { Contains, Prefix }

@Composable
fun rememberAnimatedImeBottomPadding(extra: Dp = 0.dp): Dp {
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val target = with(density) { imeBottomPx.toDp() } + extra

    val animated by animateDpAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 220),
        label = "imePadding"
    )
    return animated
}

@Preview(name = "With dropdown icon", showBackground = true, widthDp = 360)
@Composable
private fun SuggestionAutoCompleteFieldPreview() {
    var ingredientValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    SuggestionAutoCompleteField(
        value = ingredientValue,
        onValueChange = { ingredientValue = it },
        suggestions = listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
        label = "Ingredient",
        showDropdownIcon = true,
        matchMode = MatchMode.Contains
    )
}


@Preview(name = "Without dropdown icon", showBackground = true, widthDp = 360)
@Composable
private fun SuggestionAutoCompleteFieldWithoutDropdownPreview() {
    var ingredientValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    SuggestionAutoCompleteField(
        value = ingredientValue,
        onValueChange = { ingredientValue = it },
        suggestions = listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
        label = "Ingredient",
        showDropdownIcon = false,
        matchMode = MatchMode.Contains
    )
}