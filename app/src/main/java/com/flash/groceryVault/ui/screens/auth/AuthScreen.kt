@file:Suppress("DEPRECATION")

package com.flash.groceryVault.ui.screens.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.groceryVault.R
import com.flash.groceryVault.ui.components.StandardTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    vm: AuthViewModel,
    onLoggedIn: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    // Requires a real google-services.json to generate R.string.default_web_client_id
    val webClientId = stringResource(R.string.default_web_client_id)
    val googleClient = remember(context, webClientId) { googleSignInClient(webClientId, context) }


    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vm.onGoogleResult(result.resultCode, result.data)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is AuthEvent.Toast -> Toast.makeText(context, event.message, Toast.LENGTH_LONG)
                    .show()

                AuthEvent.NavigateLoggedIn -> {
                    vm.startNavigation()
                    onLoggedIn()
                }

                AuthEvent.LaunchGoogleSignIn -> googleLauncher.launch(googleClient.signInIntent)
            }
        }
    }

    AuthFormContent(
        ui = ui,
        onEmailChange = vm::onEmailChange,
        onPasswordChange = vm::onPasswordChange,
        onSignIn = vm::submitSignIn,
        onSignUp = vm::submitSignUp,
        onGoogleSignIn = { vm.onGoogleSignInClicked(webClientId) }
    )
}


fun googleSignInClient(id: String, context: Context): GoogleSignInClient = GoogleSignIn.getClient(
    context,
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .apply { if (id.isNotBlank()) requestIdToken(id) }
        .build()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthFormContent(
    ui: AuthFormUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(
                    space = 12.dp,
                    alignment = Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StandardTextField(
                    value = ui.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardType = KeyboardType.Email,
                )

                StandardTextField(
                    value = ui.password,
                    onValueChange = onPasswordChange,
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                if (ui.authState is AuthState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(60.dp))

                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sign in") }

                OutlinedButton(
                    onClick = onSignUp,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create account") }

                GoogleSignInButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (ui.isNavigating) {
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

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google_logo),
            contentDescription = "Google logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text("Sign in with Google")
    }
}

