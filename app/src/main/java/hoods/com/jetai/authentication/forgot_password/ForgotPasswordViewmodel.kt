package hoods.com.jetai.authentication.forgot_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hoods.com.jetai.Graph
import hoods.com.jetai.data.repository.AuthRepository
import hoods.com.jetai.utils.ext.collectAndHandle
import hoods.com.jetai.utils.isValidEmail
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    val repository: AuthRepository = Graph.authRepository,
) : ViewModel() {
    var forgotPasswordState by mutableStateOf(ForgotPasswordState())
        private set

    fun forgotPasswordEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.SendForgotPasswordLink -> {
                viewModelScope.launch {
                    forgotPasswordState = forgotPasswordState.copy(
                        errorMsg = null
                    )
                    try {
                        if (!isValidEmail(forgotPasswordState.email)) {
                            throw IllegalArgumentException("Invalid Email Address")
                        }
                        repository.sendPasswordResetLink(forgotPasswordState.email)
                            .collectAndHandle(
                                onError = {
                                    forgotPasswordState = forgotPasswordState.copy(
                                        isPasswordLinkSent = false,
                                        isLoading = false,
                                        errorMsg = it?.localizedMessage
                                    )
                                },
                                onLoading = {
                                    forgotPasswordState = forgotPasswordState.copy(
                                        isLoading = true,
                                    )
                                }
                            ) {
                                forgotPasswordState = forgotPasswordState.copy(
                                    isPasswordLinkSent = true,
                                    isLoading = false
                                )
                            }

                    } catch (e: Exception) {
                        forgotPasswordState = forgotPasswordState.copy(
                            errorMsg = e.localizedMessage
                        )
                    }
                }
            }

            is ForgotPasswordEvent.OnEmailChange -> {
                forgotPasswordState = forgotPasswordState.copy(
                    email = event.email
                )
            }
        }
    }


}

data class ForgotPasswordState(
    val email: String = "",
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val isPasswordLinkSent: Boolean = false,
)

sealed class ForgotPasswordEvent {
    data object SendForgotPasswordLink : ForgotPasswordEvent()
    data class OnEmailChange(val email: String) : ForgotPasswordEvent()
}