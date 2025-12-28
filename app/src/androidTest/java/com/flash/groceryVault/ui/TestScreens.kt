package com.flash.groceryVault.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flash.groceryVault.data.GroceryRepository
import com.flash.groceryVault.ui.screens.groceryList.GroceryListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreenForTest(
    repo: GroceryRepository
) {
    val vm = remember { GroceryListViewModel(repo) }
    val grocerys by vm.grocerys.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Grocerys") }) }
    ) { padding ->
        if (grocerys.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("No grocerys yet. Tap + to add one.")
            }
        }
    }
}
