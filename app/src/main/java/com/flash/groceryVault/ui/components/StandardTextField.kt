package com.flash.groceryVault.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

@Composable
fun StandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
    )
}

@Preview(name = "Standard TextField Preview", showBackground = true, widthDp = 360)
@Composable
private fun StandardTextFieldPreview() {
    GroceryVaultTheme {
        StandardTextField(
            value = "Sample Text",
            onValueChange = {},
            label = "Sample Label"
        )
    }
}

@Preview(name = "Standard TextField Number Preview", showBackground = true, widthDp = 360)
@Composable
private fun StandardTextFieldNumberPreview() {
    GroceryVaultTheme {
        StandardTextField(
            value = "123",
            onValueChange = {},
            label = "Quantity",
            keyboardType = KeyboardType.Number
        )
    }
}
