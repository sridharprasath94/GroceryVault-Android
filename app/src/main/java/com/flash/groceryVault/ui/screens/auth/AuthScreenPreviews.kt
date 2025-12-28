package com.flash.groceryVault.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.flash.groceryVault.ui.components.StandardTextField
import com.flash.groceryVault.ui.theme.GroceryVaultTheme


@Preview(name = "Auth Form - Logged Out", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun AuthFormContentPreviewLoggedOut() {
    GroceryVaultTheme {
        Scaffold { padding ->
            AuthFormContent(
                padding = padding,
                email = "",
                onEmailChange = {},
                password = "",
                onPasswordChange = {},
                state = AuthState.LoggedOut,
                onSignIn = {},
                onSignUp = {},
                onGoogleSignIn = {},
            )
        }
    }
}

@Preview(name = "Auth Form - Loading", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun AuthFormContentPreviewLoading() {
    GroceryVaultTheme {
        Scaffold { padding ->
            AuthFormContent(
                padding = padding,
                email = "user@example.com",
                onEmailChange = {},
                password = "password",
                onPasswordChange = {},
                state = AuthState.Loading,
                onSignIn = {},
                onSignUp = {},
                onGoogleSignIn = {},
            )
        }
    }
}

@Preview(name = "Email Field", showBackground = true)
@Composable
fun StandardTextFieldEmailPreview() {
    StandardTextField(
        value = "user@example.com",
        onValueChange = {},
        label = "Email",
        keyboardType = KeyboardType.Email,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(name = "Password Field", showBackground = true)
@Composable
fun StandardTextFieldPasswordPreview() {
    StandardTextField(
        value = "password",
        onValueChange = {},
        label = "Password",
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
}
