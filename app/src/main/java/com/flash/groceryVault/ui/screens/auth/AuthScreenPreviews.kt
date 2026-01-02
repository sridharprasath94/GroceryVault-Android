package com.flash.groceryVault.ui.screens.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.StandardTextField
import com.flash.groceryVault.ui.theme.GroceryVaultTheme


private fun previewAuthFormUiState(authState: AuthState = AuthState.LoggedOut): AuthFormUiState {
    return AuthFormUiState(
        email = "abc-user@gmail.com",
        password = "password123",
        authState = authState,
        isNavigating = false
    )
}

@Composable
private fun AuthFormContentPreviewWrapper(
    ui: AuthFormUiState
) {
    GroceryVaultTheme {
        AuthFormContent(
            ui = ui,
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onSignUp = {},
            onGoogleSignIn = {},
        )
    }
}

@Preview(
    name = "Auth Form - Logged Out",
    showBackground = true, widthDp = 360, heightDp = 720
)
@Preview(
    name = "Auth Form - Logged Out - Dark",
    showBackground = true, widthDp = 360, heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AuthFormContentPreviewLoggedOut() {
    AuthFormContentPreviewWrapper(
        ui = previewAuthFormUiState(),
    )
}

@Preview(name = "Auth Form - Loading", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(
    name = "Auth Form - Loading - Dark",
    showBackground = true, widthDp = 360, heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AuthFormContentPreviewLoading() {
    GroceryVaultTheme {
        AuthFormContentPreviewWrapper(
            ui = previewAuthFormUiState(authState = AuthState.Loading),
        )
    }
}

@Preview(name = "Auth Form - Logged In", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(
    name = "Auth Form - Logged In - Dark",
    showBackground = true, widthDp = 360, heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AuthFormContentPreviewLoggedIn() {
    AuthFormContentPreviewWrapper(
        ui = previewAuthFormUiState(
            authState = AuthState.LoggedIn(
                "userId123",
                "abc-user@gmail.com"
            )
        ),
    )
}

@Preview(name = "Email Field", showBackground = true)
@Preview(
    name = "Email Field - Dark", showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StandardTextFieldEmailPreview() {
    GroceryVaultTheme {
        StandardTextField(
            value = "user@example.com",
            onValueChange = {},
            label = "Email",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Password Field", showBackground = true)
@Preview(
    name = "Password Field - Dark", showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StandardTextFieldPasswordPreview() {
    GroceryVaultTheme {
        StandardTextField(
            value = "password",
            onValueChange = {},
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}