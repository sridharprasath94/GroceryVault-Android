package com.flash.groceryVault.ui.components


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    )
}

@Preview(name = "Confirmation Dialog Preview", showBackground = true, widthDp = 360)
@Composable
private fun ConfirmationDialogPreview() {
    ConfirmationDialog(
        show = true,
        title = "Delete Grocery List",
        message = "Are you sure you want to delete this grocery?",
        confirmButtonText = "Delete",
        onConfirm = {},
        onDismiss = {}
    )
}
