package com.flash.groceryVault.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTopBar(
    title: String,
    actionLabel: String,
    isInteractionEnabled: Boolean,
    isActionInProgress: Boolean = false,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    Box {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = isInteractionEnabled) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = onPrimaryAction,
                    enabled = isInteractionEnabled && !isActionInProgress
                ) {
                    if (isActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(actionLabel)
                    }
                }
            }
        )

        if (!isInteractionEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) { /* block touches */ }
            )
        }
    }
}

@Preview
@Composable
fun FormTopBarPreview() {
    FormTopBar(
        title = "Edit Recipe",
        actionLabel = "Save",
        isInteractionEnabled = true,
        onBack = {},
        onPrimaryAction = {}
    )
}
