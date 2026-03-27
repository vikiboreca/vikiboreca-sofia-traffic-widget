package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.Login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.widget_kotlin.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class loginBody {
    @Composable
    fun LoginScreen(onLoginSuccess: () -> Unit) {
        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            if (auth.currentUser != null) onLoginSuccess()
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                scope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(
                                GetGoogleIdOption.Builder()
                                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()
                            )
                            .build()

                        val result = credentialManager.getCredential(context, request)
                        val googleIdToken = GoogleIdTokenCredential
                            .createFrom(result.credential.data).idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { onLoginSuccess() }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }) {
                Text("Sign in with Google")
            }
        }
    }
}