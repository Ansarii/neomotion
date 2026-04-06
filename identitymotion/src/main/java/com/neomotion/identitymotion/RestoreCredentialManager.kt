package com.neoninnovationlab.neomotion.identitymotion

import android.content.Context
import android.util.Log
import androidx.credentials.CreateRestoreCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.RestoreCredential
import androidx.credentials.exceptions.CreateCredentialException

/**
 * Domain component responsible for silent, zero-click logins across device restores.
 *
 * Wraps the Android Jetpack Restore Credentials API (CredentialManager 1.5.0+).
 *
 * ## How Restore Credentials work (from Android docs)
 * 1. After a successful login, call [saveRestoreCredential]. This creates a FIDO2-compatible
 *    restore key backed up to the cloud (Google Backup) and persists the userId locally.
 * 2. On a new device, call [getRestoreCredential]. If the OS has the restore key,
 *    it returns it (proving identity). We read the userId from local storage.
 * 3. In a production app, the credential response would be sent to a relying party server
 *    for cryptographic verification. For this demo, existence of the credential is sufficient.
 *
 * ## Why userId isn't extracted from the credential bundle
 * Per the official Android docs: "Send the public key from the app to the relying party server,
 * which can then be used to sign in the user." The credential object contains a FIDO2 assertion
 * (public key), not plaintext user data. The server verifies the key and identifies the user.
 * For a serverless demo, we use [SharedPreferences] to simulate server-side userId lookup.
 */
class RestoreCredentialManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    /** SharedPreferences key for the persisted userId */
    private val prefs by lazy {
        context.getSharedPreferences("neo_identity_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG    = "RestoreCredentialManager"
        private const val KEY_USER_ID = "restored_user_id"
    }

    /**
     * Saves a restore credential when the user successfully signs in.
     *
     * Creates a FIDO2-compatible restore key and persists it to the OS backup.
     * Also persists [userId] to [SharedPreferences] so we can retrieve it when
     * the credential is restored on a new device (simulating server-side lookup).
     *
     * @param userId The unique identifier for the authenticated user.
     */
    suspend fun saveRestoreCredential(userId: String) {
        // Encode userId as Base64URL for the WebAuthn user.id field (must be base64url, no padding)
        val base64UserId = android.util.Base64.encodeToString(
            userId.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        )
        val challenge = android.util.Base64.encodeToString(
            "neo_motion_restore_challenge".toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        )

        // WebAuthn PublicKeyCredentialCreationOptionsJSON — strict W3C format required
        val requestJson = """
        {
          "challenge": "$challenge",
          "rp": {
            "name": "NeoMotion",
            "id": "com.neoninnovationlab.neomotion.demo"
          },
          "user": {
            "id": "$base64UserId",
            "name": "user@example.com",
            "displayName": "NeoMotion User"
          },
          "pubKeyCredParams": [
            { "type": "public-key", "alg": -7 }
          ],
          "timeout": 60000,
          "attestation": "none",
          "excludeCredentials": [],
          "authenticatorSelection": {
            "authenticatorAttachment": "platform",
            "requireResidentKey": true,
            "residentKey": "required",
            "userVerification": "required"
          }
        }
        """.trimIndent()

        try {
            val request = CreateRestoreCredentialRequest(requestJson = requestJson)
            credentialManager.createCredential(context, request)

            // Persist userId locally — in a real app the server would derive this from
            // the FIDO2 credential assertion. For the demo, we store it directly.
            prefs.edit().putString(KEY_USER_ID, userId).apply()
            Log.d(TAG, "Restore credential saved for user: $userId")
        } catch (e: CreateCredentialException) {
            // Likely E2eeUnavailableException: device has no screen lock / backup disabled.
            // Still persist locally so the demo works on emulators without backup enabled.
            prefs.edit().putString(KEY_USER_ID, userId).apply()
            Log.w(TAG, "Credential save failed (device may lack E2EE backup). UserId persisted locally.", e)
        }
    }

    /**
     * Retrieves the backed-up restore credential on a new device.
     *
     * If the OS returns a [RestoreCredential], the user is authenticated —
     * we return the previously persisted userId (simulating server lookup).
     *
     * @return The restored userId, or null if no credential was found.
     */
    suspend fun getRestoreCredential(): String? {
        val challenge = android.util.Base64.encodeToString(
            "neo_motion_get_challenge".toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
        )

        // WebAuthn PublicKeyCredentialRequestOptionsJSON
        val requestJson = """
        {
          "challenge": "$challenge",
          "timeout": 60000,
          "rpId": "com.neoninnovationlab.neomotion.demo",
          "userVerification": "required",
          "allowCredentials": []
        }
        """.trimIndent()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                androidx.credentials.GetRestoreCredentialOption(requestJson = requestJson)
            )
            .build()

        return try {
            val result     = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is RestoreCredential) {
                // Credential exists — user is authenticated.
                // Read the userId from SharedPreferences (simulating server-side lookup).
                val userId = prefs.getString(KEY_USER_ID, null)
                Log.d(TAG, "Restore credential found. UserId: $userId")
                userId
            } else {
                Log.w(TAG, "Unexpected credential type returned: ${credential.type}")
                null
            }
        } catch (e: Exception) {
            // NoCredentialException: no restore key found on this device (expected on first install).
            // All other exceptions: log and degrade gracefully.
            Log.d(TAG, "No restore credential found: ${e.message}")
            null
        }
    }

    /**
     * Clears the restore credential state (call on user logout).
     * Also removes the persisted userId.
     */
    suspend fun clearRestoreCredential() {
        prefs.edit().remove(KEY_USER_ID).apply()
        try {
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest(
                    androidx.credentials.ClearCredentialStateRequest.TYPE_CLEAR_RESTORE_CREDENTIAL
                )
            )
            Log.d(TAG, "Restore credential cleared.")
        } catch (e: Exception) {
            Log.w(TAG, "clearCredentialState failed: ${e.message}")
        }
    }
}
