package com.ssrlab.assistant.client

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ssrlab.assistant.R
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.ui.login.LaunchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthClient(
    private val context: Context,
    private val mainApp: MainApplication,
    private val sharedPreferences: SharedPreferences
) {

    private val fireAuth = FirebaseAuth.getInstance()
    private val emailRegex = Regex("^[a-zA-Z0-9][a-zA-Z0-9.]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9][a-zA-Z0-9]+")

    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * 0 - FireAuth error;
     * 1 - Login error;
     * 2 - Password error
     */
    fun signUp(login: String, password: String, onSuccess: () -> Unit, onFailure: (String, Int) -> Unit) {
        if (login.matches(emailRegex)) {
            if (password.length >= 8) {
                fireAuth.createUserWithEmailAndPassword(login, password).addOnSuccessListener {
                    fireAuth.signInWithEmailAndPassword(login, password)
                        .addOnSuccessListener {
                            sendVerificationEmail(
                                {
                                    mainApp.saveUserSignedIn(sharedPreferences, isUserSigned = true, email = login, password = password)
                                    onSuccess()
                                },
                                { onFailure(it, 0) }
                            )
                        }
                        .addOnFailureListener { onFailure(it.message!!, 0) }
                }.addOnFailureListener { onFailure(it.message!!, 0) }
            } else {
                val errMsg = ContextCompat.getString(context, R.string.password_length_error)
                onFailure(errMsg, 2)
            }
        } else {
            val errMsg = ContextCompat.getString(context, R.string.email_type_error)
            onFailure(errMsg, 1)
        }
    }

    fun sendVerificationEmail(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        fireAuth.currentUser?.sendEmailVerification()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onFailure(it.message.toString()) }
    }

    fun awaitIfUserIsVerified(scope: CoroutineScope, onSuccess: () -> Unit) {
        scope.launch {
            while (fireAuth.currentUser?.isEmailVerified != true) {
                delay(1000)
                fireAuth.currentUser?.reload()
            }

            onSuccess()
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        fireAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message.toString()) }
    }

    fun checkIfUserExists(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        fireAuth.createUserWithEmailAndPassword(email, "      ")
            .addOnSuccessListener {
                fireAuth.currentUser?.delete()
                onFailure()
            }
            .addOnFailureListener { onSuccess() }
    }

    fun deleteUser(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        fireAuth.currentUser?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onFailure(it.message.toString()) }
    }

    fun automateSignIn(activity: LaunchActivity, dialog: Dialog, onSuccess: () -> Unit) {
        mainApp.getIsUserSignObject().apply {
            if (isSignedIn) {
                dialog.show()

                if (!isGoogle) {
                    fireAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            dialog.dismiss()
                            onSuccess()
                        }
                } else {
                    generateSignInClient(activity)
                    val task = googleSignInClient?.silentSignIn()
                    if (task?.isSuccessful == true) {
                        dialog.dismiss()
                        onSuccess()
                    } else {
                        dialog.dismiss()
                    }
                }
            } else {
                dialog.dismiss()
            }
        }
    }

    fun signIn(login: String, password: String, onSuccess: (AuthResult) -> Unit, onFailure: (String, Int) -> Unit, onVerification: () -> Unit) {
        if (login.matches(emailRegex)) {
            if (password.length >= 8) {
                fireAuth.signInWithEmailAndPassword(login, password)
                    .addOnSuccessListener {
                        if (fireAuth.currentUser?.isEmailVerified == true) {
                            mainApp.saveUserSignedIn(sharedPreferences, isUserSigned = true, isGoogle = false, email = it.user?.email!!, password = password)
                            onSuccess(it)
                        }
                        else onVerification()
                    }
                    .addOnFailureListener { onFailure(it.message!!, 0) }
            } else {
                val errMsg = ContextCompat.getString(context, R.string.password_length_error)
                onFailure(errMsg, 2)
            }
        } else {
            val errMsg = ContextCompat.getString(context, R.string.email_type_error)
            onFailure(errMsg, 1)
        }
    }

    fun signIn(activity: LaunchActivity, dialog: Dialog, onSuccess: (AuthResult) -> Unit, onFailure: (String, Int) -> Unit) {
        generateSignInClient(activity)
        val intent = googleSignInClient!!.signInIntent

        googleSignInClient!!.signOut().addOnCompleteListener {
            activity.googleIntent { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(dialog, task, { onSuccess(it) }, { msg, type -> onFailure(msg, type) })
            }

            activity.getLauncher().launch(intent)
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        fireAuth.signOut()
        onSuccess()
    }

    private fun generateSignInClient(activity: LaunchActivity) {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.google_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(activity, gso)
        }
    }

    private fun handleGoogleSignInResult(dialog: Dialog, task: Task<GoogleSignInAccount>, onSuccess: (AuthResult) -> Unit,onFailure: (String, Int) -> Unit) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null)
                googleSignInWithCredential(dialog, account, { onSuccess(it) }, { msg, type -> onFailure(msg, type) })
        } else
            onFailure(task.exception.toString(), 0)
    }

    private fun googleSignInWithCredential(dialog: Dialog, account: GoogleSignInAccount, onSuccess: (AuthResult) -> Unit, onFailure: (String, Int) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        dialog.show()

        fireAuth.signInWithCredential(credential).addOnSuccessListener {
            mainApp.saveUserSignedIn(sharedPreferences, isUserSigned = true, isGoogle = true)

            dialog.dismiss()
            onSuccess(it)
        }.addOnFailureListener {
            dialog.dismiss()

            val errMsg = it.message.toString()
            onFailure(errMsg, 0)
        }
    }

    fun isEmailValid(email: String) = email.matches(emailRegex)
}

data class IsUserSignedIn(
    var isSignedIn: Boolean = false,
    var isGoogle: Boolean = false,
    var email: String = "",
    var password: String = "",
)