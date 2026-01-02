@file:Suppress("DEPRECATION")

package com.flash.groceryVault.ui.screens.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed interface AuthEvent {
    data class Toast(val message: String) : AuthEvent
    object NavigateLoggedIn : AuthEvent
    object LaunchGoogleSignIn : AuthEvent
}

sealed class AuthState {
    data object Loading : AuthState()
    data object LoggedOut : AuthState()
    data class LoggedIn(val uid: String, val email: String?) : AuthState()
}

data class AuthFormUiState(
    val email: String = "",
    val password: String = "",
    val isNavigating: Boolean = false,
    val authState: AuthState = AuthState.Loading
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthFormUiState())
    val ui: StateFlow<AuthFormUiState> = _ui

    private val _events = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private val authListener = FirebaseAuth.AuthStateListener { updateAuthState() }

    init {
        updateAuthState()
        auth.addAuthStateListener(authListener)
    }

    fun onEmailChange(v: String) {
        _ui.value = _ui.value.copy(email = v)
    }

    fun onPasswordChange(v: String) {
        _ui.value = _ui.value.copy(password = v)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    private fun updateAuthState() {
        val user = auth.currentUser
        _ui.update { it ->
            it.copy(authState = user?.let {
                AuthState.LoggedIn(it.uid, it.email)
            }
                ?: AuthState.LoggedOut)
        }
        user?.let { emitIfAllowed(AuthEvent.NavigateLoggedIn) }
    }


    fun signIn(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            toast("Email and password are required")
            return@launch
        }

        try {
            _ui.update { it.copy(authState = AuthState.Loading) }
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()

        } catch (e: Exception) {
            toast(e.message ?: "Sign-in failed")
        }
    }

    fun signUp(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            toast("Email and password are required")
            return@launch
        }

        try {
            _ui.update { it.copy(authState = AuthState.Loading) }
            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
        } catch (e: Exception) {
            toast(e.message ?: "Sign-up failed")
        }
    }

    fun submitSignIn() = signIn(ui.value.email, ui.value.password)

    fun submitSignUp() = signUp(ui.value.email, ui.value.password)

    fun signInWithGoogleIdToken(idToken: String) = viewModelScope.launch {
        try {
            _ui.update { it.copy(authState = AuthState.Loading) }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google sign-in failed", e)
            toast(e.message ?: "Google sign-in failed")
        }
    }

    fun onGoogleResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            toast(
                if (resultCode == Activity.RESULT_CANCELED) {
                    "Google sign-in was cancelled."
                } else {
                    "Google sign-in failed (resultCode=$resultCode)."
                }
            )
            return
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                toast("Google sign-in returned blank idToken. Check default_web_client_id + SHA-1.")
                return
            }
            signInWithGoogleIdToken(idToken)
        } catch (e: ApiException) {
            toast("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        } catch (e: Exception) {
            toast("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun onGoogleSignInClicked(webClientId: String) {
        if (webClientId.isBlank()) {
            toast("Google Sign-In is not configured. Please set up Firebase and provide a valid web client ID.")
        } else {
            emitIfAllowed(AuthEvent.LaunchGoogleSignIn)
        }
    }

    private fun toast(message: String) {
        emitIfAllowed(AuthEvent.Toast(message))
    }


    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }


    private fun emitIfAllowed(event: AuthEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)
        }
    }
}

