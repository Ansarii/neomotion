package com.neoninnovationlab.neomotion.demo.features.identity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neoninnovationlab.neomotion.identitymotion.RestoreCredentialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Checking : AuthState()
    object LoggedOut : AuthState()
    data class LoggedIn(val userId: String, val isRestored: Boolean) : AuthState()
}

@HiltViewModel
class SilentLoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val restoreManager = RestoreCredentialManager(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // As soon as the ViewModel initializes (simulating app open), we check for a restore key
        checkRestoreCredential()
    }

    private fun checkRestoreCredential() {
        viewModelScope.launch {
            _authState.value = AuthState.Checking
            val restoredUserId = restoreManager.getRestoreCredential()
            
            if (restoredUserId != null) {
                // We silently got a backed up credential! The user didn't have to do anything.
                _authState.value = AuthState.LoggedIn(userId = restoredUserId, isRestored = true)
            } else {
                // No credential found, show login UI
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun simulateManualLogin() {
        val fakeUserId = "User_10495_Neo"
        viewModelScope.launch {
            _authState.value = AuthState.Checking
            
            // Generate and save the restore credential to the system Block Store natively.
            restoreManager.saveRestoreCredential(fakeUserId)
            
            _authState.value = AuthState.LoggedIn(userId = fakeUserId, isRestored = false)
        }
    }

    fun simulateDeviceWipe() {
        // Simulating the user uninstalling the app or wiping it cleanly.
        // We do NOT clear the system block store, to exactly replicate
        // reinstalling the app from a Google One backup.
        _authState.value = AuthState.LoggedOut
    }

    fun triggerSilentRestoreCheck() {
        checkRestoreCredential()
    }
}
