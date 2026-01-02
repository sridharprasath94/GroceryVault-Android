package com.flash.groceryVault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flash.groceryVault.di.AppContainer
import com.flash.groceryVault.ui.screens.auth.AuthScreen
import com.flash.groceryVault.ui.screens.auth.AuthState
import com.flash.groceryVault.ui.screens.auth.AuthViewModel
import com.flash.groceryVault.ui.screens.createGrocery.CreateGroceryScreen
import com.flash.groceryVault.ui.screens.createGrocery.CreateGroceryViewModel
import com.flash.groceryVault.ui.screens.detailGrocery.GroceryDetailScreen
import com.flash.groceryVault.ui.screens.detailGrocery.GroceryDetailViewModel
import com.flash.groceryVault.ui.screens.editGrocery.EditGroceryScreen
import com.flash.groceryVault.ui.screens.editGrocery.EditGroceryViewModel
import com.flash.groceryVault.ui.screens.listGrocery.GroceryListScreen
import com.flash.groceryVault.ui.screens.listGrocery.GroceryListViewModel

object Routes {
    const val AUTH = "auth"
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAIL = "detail"
    const val EDIT = "edit"
}

@Composable
fun AppRoot(container: AppContainer) {
    val nav = rememberNavController()

    val authVm = remember { AuthViewModel() }
    val authState = authVm.ui.collectAsState().value.authState

    // seed suggestion defaults once (after first composition)
    LaunchedEffect(Unit) {
        runCatching { container.seedSuggestionDefaultsIfEmpty() }
    }

    // Start/stop realtime sync while logged in
    DisposableEffect(authState) {
        val sync = runCatching { container.firestoreSyncServiceForCurrentUser() }.getOrNull()
        if (authState is AuthState.LoggedIn && sync != null) {
            sync.startRealTime()
            onDispose { sync.stopRealTime() }
        } else {
            onDispose { }
        }
    }

    NavHost(
        navController = nav,
        startDestination = if (authState is AuthState.LoggedIn) Routes.LIST else Routes.AUTH
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                vm = authVm,
                onLoggedIn = {
                    nav.navigate(Routes.LIST) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIST) {
//           LocalContext.current.deleteDatabase( "grocery_db_${(authState as? AuthState.LoggedIn)?.uid}")
            val vm = remember { GroceryListViewModel(container) }
            GroceryListScreen(
                vm = vm,
                onCreate = { nav.navigate(Routes.CREATE) },
                onOpenGroceryItem = { id -> nav.navigate("${Routes.DETAIL}/$id") },
                onEditGroceryItem = { id -> nav.navigate("${Routes.EDIT}/$id") },
                onLoggedOut = {
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CREATE) {
            val vm = remember {
                CreateGroceryViewModel(
                    container.groceryRepositoryForCurrentUser,
                    container.suggestionsRepository,
                )
            }
            CreateGroceryScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onCreated = { _ ->
                    nav.popBackStack()
                }
            )
        }

        composable(
            route = "${Routes.DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            val vm = remember {
                GroceryDetailViewModel(
                    container.groceryRepositoryForCurrentUser,
                    id,
                )
            }
            GroceryDetailScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("${Routes.EDIT}/$id") }
            )
        }

        composable(
            route = "${Routes.EDIT}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("id") ?: return@composable
            val vm = remember {
                EditGroceryViewModel(
                    container.groceryRepositoryForCurrentUser,
                    container.suggestionsRepository,
                    id,
                )
            }
            EditGroceryScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() }
            )
        }
    }
}
