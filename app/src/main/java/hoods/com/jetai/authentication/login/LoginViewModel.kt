package hoods.com.jetai.authentication.login

import android.content.Intent
import android.content.IntentSender
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import hoods.com.jetai.Graph
import hoods.com.jetai.data.repository.AuthRepository
import hoods.com.jetai.data.repository.GoogleAuthClient
import hoods.com.jetai.utils.ext.collectAndHandle
import hoods.com.jetai.utils.isValidEmail
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = Graph.authRepository,
    private val googleAuthClient: GoogleAuthClient = Graph.googleAuthClient,
) : ViewModel() {
    var loginState by mutableStateOf(LoginState())
        private set

    companion object {
        const val TAG = "loginVm"
    }

    init {
        viewModelScope.launch {
            repository.currentUser.collectLatest {
                loginState = loginState.copy(
                    currentUser = it
                )
            }
        }
    }

    fun loginEvent(loginEvent: LoginEvents) {
        when (loginEvent) {
            is LoginEvents.OnEmailChange -> {
                loginState = loginState.copy(
                    email = loginEvent.email
                )
            }

            is LoginEvents.OnPasswordChange -> {
                loginState = loginState.copy(
                    password = loginEvent.password
                )
            }

            is LoginEvents.Login -> {
                login()
            }

            is LoginEvents.OnResendVerification -> {
                resendVerification()
            }

            is LoginEvents.SignInWithGoogle -> {
                viewModelScope.launch {
                    googleAuthClient.signInWithIntent(loginEvent.intent)
                        .collectAndHandle(
                            onError = {
                                loginState = loginState.copy(
                                    loginErrorMsg = it?.localizedMessage
                                )
                            },
                            onLoading = {
                                loginState = loginState.copy(isLoading = true)
                            }
                        ) {
                            hasNotVerifiedThrowError()
                            loginState = loginState.copy(
                                isSuccessLogin = true,
                                isLoading = false
                            )
                        }
                }
            }

            is LoginEvents.LogOut -> {
                repository.signOut()
            }
        }
    }

    private fun validateLoginForm() =
        loginState.email.isNotBlank() && loginState.password.isNotBlank()

    private fun resendVerification() {
        try {
            repository.sendVerificationEmail(
                onSuccess = {
                    loginState = loginState.copy(showResendBtn = false)
                },
                onError = {
                    loginState = loginState.copy(
                        loginErrorMsg = it?.localizedMessage
                    )
                }
            )

        } catch (e: Exception) {
            loginState = loginState.copy(
                loginErrorMsg = e.localizedMessage
            )
            e.printStackTrace()
        }
    }

    private fun login() = viewModelScope.launch {
        try {
            loginState = loginState.copy(
                loginErrorMsg = null
            )
            if (!validateLoginForm()) throw IllegalArgumentException("Password or Email can not be empty")
            if (!isValidEmail(loginState.email)) throw IllegalArgumentException("Invalid Email Address")
            loginState = loginState.copy(
                isLoading = true
            )
            repository.login(loginState.email, loginState.password)
                .collectAndHandle(
                    onLoading = {
                        loginState = loginState.copy(
                            isLoading = true
                        )
                    },
                    onError = {
                        loginState = loginState.copy(
                            isSuccessLogin = false,
                            isLoading = false,
                            loginErrorMsg = it?.localizedMessage
                        )
                    }
                ) {
                    hasNotVerifiedThrowError()
                    loginState = loginState.copy(
                        isSuccessLogin = true,
                        isLoading = false,
                    )
                }
        } catch (e: Exception) {
            loginState = loginState.copy(
                loginErrorMsg = e.localizedMessage
            )
        } finally {
            loginState = loginState.copy(
                isLoading = false
            )
        }
    }

    private fun hasNotVerifiedThrowError() {
        if (!repository.hasVerifiedUser()) {
            loginState = loginState.copy(showResendBtn = true)
            throw IllegalArgumentException(
                """
                 We've sent a verification link to your email. 
                 Please check your inbox and click the link to activate your account.${loginState.currentUser?.email}
            """.trimIndent()
            )
        }
    }

    fun hasUserVerified(): Boolean = repository.hasUser() && repository.hasVerifiedUser()

    suspend fun signInWithGoogleIntentSender(): IntentSender? = googleAuthClient.signIn()

}


data class LoginState(
    val loginErrorMsg: String? = null,
    val isLoading: Boolean = false,
    val isValidEmailAddress: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isSuccessLogin: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val isUSerVerified: Boolean? = null,
    val showResendBtn: Boolean = false,
)

sealed class LoginEvents {
    data class OnEmailChange(val email: String) : LoginEvents()
    data class OnPasswordChange(val password: String) : LoginEvents()
    data object Login : LoginEvents()
    data object OnResendVerification : LoginEvents()
    data class SignInWithGoogle(
        val intent: Intent,
    ) : LoginEvents()

    data object LogOut : LoginEvents()
}













