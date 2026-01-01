package com.flash.groceryVault.ui.screens.editGrocery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.groceryVault.ui.components.FormTopBar
import com.flash.groceryVault.ui.components.GroceryForm
import com.flash.groceryVault.ui.components.GroceryItemFormRow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rememberAnimatedImeBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroceryScreen(
    vm: EditGroceryViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is EditGroceryEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is EditGroceryEvent.OnFinishedSaving -> {
                    vm.startNavigation()
                    onSaved()
                }

                EditGroceryEvent.OnBackClicked -> {
                    vm.startNavigation()
                    onBack()
                }
            }
        }
    }

    EditGroceryForm(
        ui = ui,
        onBack = vm::requestBack,
        onSave = {
            keyboardController?.hide()
            vm.save()
        },
        onTitleChange = vm::updateTitle,
        onDescriptionChange = vm::updateDescription,
        onItemChange = vm::onGroceryItemChanged,
        onItemRemove = vm::onGroceryItemRemoved,
        onAddItem = {
            vm.onAddGroceryItem()
            scope.launch {
                listState.animateScrollToItem(ui.groceryItems.lastIndex)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroceryForm(
    ui: EditGroceryUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onItemChange: (Int, GroceryItemFormRow) -> Unit,
    onItemRemove: (Int) -> Unit,
    onAddItem: () -> Unit,
) {
    val imePadding = rememberAnimatedImeBottomPadding()
    val isInteractionEnabled = !ui.isSaving && !ui.isLoadingData && !ui.isNavigating

    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            FormTopBar(
                title = "Edit Grocery List",
                actionLabel = "Save",
                isInteractionEnabled = isInteractionEnabled,
                isActionInProgress = ui.isSaving,
                onBack = onBack,
                onPrimaryAction = onSave
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            GroceryForm(
                padding = padding,
                isLoading = ui.isLoadingData,
                title = ui.title,
                onTitleChange = onTitleChange,
                description = ui.description,
                onDescriptionChange = onDescriptionChange,
                groceryItems = ui.groceryItems,
                suggestions = ui.suggestions,
                onItemChange = onItemChange,
                onItemRemove = onItemRemove,
                onAddItem = onAddItem,
            )

            if (ui.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                        )
                )
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
