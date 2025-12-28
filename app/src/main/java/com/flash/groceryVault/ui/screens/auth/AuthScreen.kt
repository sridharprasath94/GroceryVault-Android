@file:Suppress("DEPRECATION")

package com.flash.groceryVault.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.flash.groceryVault.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    vm: AuthViewModel,
    onLoggedIn: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val webClientId = stringResource(R.string.default_web_client_id)

    val googleClient = remember(webClientId) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .apply { if (webClientId.isNotBlank()) requestIdToken(webClientId) }
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w("AuthScreen", "Google sign-in canceled. resultCode=${result.resultCode}")
            vm.setError("Google sign-in cancelled.")
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                vm.setError("Google sign-in returned empty token. Check google-services.json + SHA-1.")
                return@rememberLauncherForActivityResult
            }
            vm.signInWithGoogleIdToken(idToken)
        } catch (e: Exception) {
            vm.setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    LaunchedEffect(state) {
        if (state is AuthState.LoggedIn) onLoggedIn()
    }

    // Toast-style error (simple Snackbar)
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(state) {
        if (state is AuthState.Error) {
            snack.showSnackbar((state as AuthState.Error).message)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )

            if (state is AuthState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { vm.signIn(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign in") }

            OutlinedButton(
                onClick = { vm.signUp(email.trim(), password) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create account") }

            OutlinedButton(
                onClick = {
                    if (webClientId.isBlank()) vm.setError("Missing default_web_client_id (check google-services.json).")
                    else googleLauncher.launch(googleClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign in with Google") }
        }
    }
}
