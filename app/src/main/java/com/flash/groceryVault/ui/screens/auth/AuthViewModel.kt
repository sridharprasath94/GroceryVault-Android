package com.flash.groceryVault.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    data object LoggedOut : AuthState()
    data object Loading : AuthState()
    data class LoggedIn(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val state: StateFlow<AuthState> = _state

    init {
        refresh()
        auth.addAuthStateListener { refresh() }
    }

    private fun refresh() {
        val user = auth.currentUser
        _state.value = if (user != null) AuthState.LoggedIn(user.uid) else AuthState.LoggedOut
    }

    fun signIn(email: String, password: String): Job = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
        }.onFailure {
            _state.value = AuthState.Error(it.message ?: "Sign in failed")
        }.onSuccess {
            refresh()
        }
    }

    fun signUp(email: String, password: String): Job = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await()
        }.onFailure {
            _state.value = AuthState.Error(it.message ?: "Sign up failed")
        }.onSuccess {
            refresh()
        }
    }

    fun signInWithGoogleIdToken(idToken: String): Job = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching {
            val cred = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(cred).await()
        }.onFailure {
            _state.value = AuthState.Error(it.message ?: "Google sign-in failed")
        }.onSuccess {
            refresh()
        }
    }

    fun signOut() {
        auth.signOut()
        refresh()
    }

    fun clearError() {
        if (_state.value is AuthState.Error) refresh()
    }

    fun setError(message: String) {
        _state.value = AuthState.Error(message)
    }
}

