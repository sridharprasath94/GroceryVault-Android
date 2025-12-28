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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val message: String? = null,
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form

    private val _events = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    // Cache last known logged-in user to avoid re-emitting navigation repeatedly
    private var cachedUid: String? = null

    fun onEmailChange(v: String) {
        _form.value = _form.value.copy(email = v, message = null)
    }

    fun onPasswordChange(v: String) {
        _form.value = _form.value.copy(password = v, message = null)
    }

    fun clearMessage() {
        if (_form.value.message != null) _form.value = _form.value.copy(message = null)
    }

    private val authListener = FirebaseAuth.AuthStateListener { refresh() }

    init {
        refresh()
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    private fun refresh() {
        val user = auth.currentUser

        if (user == null) {
            cachedUid = null
            _state.value = AuthState.LoggedOut
            return
        }

        val newUid = user.uid
        val newEmail = user.email
        val wasLoggedIn = cachedUid != null
        val uidChanged = cachedUid != newUid

        cachedUid = newUid
        _state.value = AuthState.LoggedIn(newUid, newEmail)

        if (!wasLoggedIn || uidChanged) {
            _events.tryEmit(AuthEvent.NavigateLoggedIn)
        }
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _form.value = _form.value.copy(message = "Email and password are required")
            toast("Email and password are required")
            return@launch
        }

        try {
            _state.value = AuthState.Loading
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            refresh()
        } catch (e: Exception) {
            toast(e.message ?: "Sign-in failed")
        }
    }

    fun signUp(email: String, password: String) = viewModelScope.launch {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _form.value = _form.value.copy(message = "Email and password are required")
            toast("Email and password are required")
            return@launch
        }

        try {
            _state.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            refresh()
        } catch (e: Exception) {
            toast(e.message ?: "Sign-up failed")
        }
    }

    fun submitSignIn() = signIn(form.value.email, form.value.password)

    fun submitSignUp() = signUp(form.value.email, form.value.password)

    fun signInWithGoogleIdToken(idToken: String) = viewModelScope.launch {
        try {
            _state.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            clearMessage()
            Log.d("AuthViewModel", "Google sign-in successful")
            refresh()
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
            _events.tryEmit(AuthEvent.LaunchGoogleSignIn)
        }
    }

    private fun toast(message: String) {
        _events.tryEmit(AuthEvent.Toast(message))
    }
}

