package com.flash.groceryVault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.flash.groceryVault.di.AppContainer
import com.flash.groceryVault.ui.AppRoot
import com.flash.groceryVault.ui.theme.GroceryVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GroceryVaultTheme {
                val container = remember { AppContainer(applicationContext) }
                AppRoot(container = container)
            }
        }
    }
}
