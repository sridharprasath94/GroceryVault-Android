package com.flash.groceryVault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.ui.theme.GroceryVaultTheme
import com.flash.groceryVault.util.GroceryAsyncImage
import com.flash.groceryVault.util.GroceryImage

@Composable
fun GroceryImagePicker(
    pickedImageUri: String?,
    existingImageUrl: String?,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAnyImage = !pickedImageUri.isNullOrBlank() || !existingImageUrl.isNullOrBlank()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onPickClick) {
                Text(if (!hasAnyImage) "Pick image (optional)" else "Change image")
            }

            if (hasAnyImage) {
                OutlinedButton(onClick = onRemoveClick) { Text("Remove") }
            }
        }

        when {
            !pickedImageUri.isNullOrBlank() -> {
                GroceryImage(
                    model = pickedImageUri,
                )
            }

            !existingImageUrl.isNullOrBlank() -> {
                GroceryAsyncImage(
                    model = existingImageUrl,
                )
            }
        }
    }
}

@Preview(name = "Image Section - With Image", showBackground = true, widthDp = 360)
@Composable
private fun GroceryImageSectionPreview_WithImage() {
    GroceryVaultTheme {
        GroceryImagePicker(
            pickedImageUri = "content://com.example.fake/image/1",
            existingImageUrl = "https://example.com/sample.jpg",
            onPickClick = {},
            onRemoveClick = {},
        )
    }
}

@Preview(name = "Image Section - No Image", showBackground = true, widthDp = 360)
@Composable
private fun GroceryImageSectionPreview_None() {
    GroceryVaultTheme {
        GroceryImagePicker(
            pickedImageUri = null,
            existingImageUrl = null,
            onPickClick = {},
            onRemoveClick = {},
        )
    }
}

@Preview(name = "Image Section - Picked Uri", showBackground = true, widthDp = 360)
@Composable
private fun GroceryImageSectionPreview_PickedUri() {
    GroceryVaultTheme {
        GroceryImagePicker(
            pickedImageUri = "content://com.example.fake/image/1",
            existingImageUrl = null,
            onPickClick = {},
            onRemoveClick = {},
        )
    }
}

@Preview(name = "Image Section - Existing Url", showBackground = true, widthDp = 360)
@Composable
private fun GroceryImageSectionPreview_ExistingUrl() {
    GroceryVaultTheme {
        GroceryImagePicker(
            pickedImageUri = null,
            existingImageUrl = "https://example.com/sample.jpg",
            onPickClick = {},
            onRemoveClick = {},
        )
    }
}
