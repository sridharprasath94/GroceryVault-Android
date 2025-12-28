package com.flash.groceryVault.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.ui.theme.GroceryVaultTheme


@Composable
fun GroceryEditFields(
    title: String,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
) {
    Column {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = desc,
            onValueChange = onDescChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/// Previews


@Preview(name = "Grocery Edit Fields", showBackground = true, widthDp = 360)
@Composable
private fun GroceryBasicFieldsPreview() {
    GroceryVaultTheme {
        GroceryEditFields(
            title = "Masala Omelette",
            onTitleChange = {},
            desc = "Eggs with onion, chili, coriander.",
            onDescChange = {},
        )
    }
}

@Preview(name = "Grocery Edit Fields - Empty", showBackground = true, widthDp = 360)
@Composable
private fun GroceryBasicFieldsEmptyPreview() {
    GroceryVaultTheme {
        GroceryEditFields(
            title = "",
            onTitleChange = {},
            desc = "",
            onDescChange = {},
        )
    }
}
