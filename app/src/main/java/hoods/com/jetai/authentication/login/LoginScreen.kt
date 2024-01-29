package hoods.com.jetai.authentication.login

import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import hoods.com.jetai.authentication.components.AlternativeLoginOptions
import hoods.com.jetai.authentication.components.HeaderText
import hoods.com.jetai.authentication.components.LoadingView
import hoods.com.jetai.authentication.components.LoginTextField
import hoods.com.jetai.authentication.login.LoginViewModel.Companion.TAG
import hoods.com.jetai.authentication.register.defaultPadding
import hoods.com.jetai.authentication.register.itemSpacing
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    isVerificationEmailSent: Boolean,
    onSignUpClick: () -> Unit,
    navigateToHomeScreen: () -> Unit,
    onForgotPasswordClick: () -> Unit,
) {
    val loginState = viewModel.loginState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                scope.launch {
                    viewModel.loginEvent(
                        LoginEvents.SignInWithGoogle(result.data ?: return@launch)
                    )
                }
            }
        }
    )
    LaunchedEffect(isVerificationEmailSent) {
        if (isVerificationEmailSent) {
            snackbarHostState.showSnackbar(
                "Verification Email Sent to ${loginState.currentUser?.email}"
            )
        }
    }
    Log.i(TAG, "LoginScreen: ${viewModel.hasUserVerified()}")
    LaunchedEffect(viewModel.hasUserVerified()) {
        Log.i(TAG, "LoginScreen: ${viewModel.hasUserVerified()}")
        if (viewModel.hasUserVerified()) {
            navigateToHomeScreen()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(defaultPadding)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column {
                HeaderText(
                    "Login",
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(vertical = defaultPadding)
                )
                AnimatedVisibility(
                    loginState.loginErrorMsg != null
                ) {
                    Text(
                        loginState.loginErrorMsg ?: "unknown",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AnimatedVisibility(loginState.showResendBtn) {
                    TextButton(
                        onClick = {
                            viewModel.loginEvent(
                                LoginEvents.OnResendVerification
                            )
                        }
                    ) {
                        Text("Resend Verification")
                    }
                }
                LoginTextField(
                    value = loginState.email,
                    onValueChange = {
                        viewModel.loginEvent(LoginEvents.OnEmailChange(it))
                    },
                    labelText = "Username",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(itemSpacing))
                LoginTextField(
                    value = loginState.password,
                    onValueChange = {
                        viewModel.loginEvent(LoginEvents.OnPasswordChange(it))
                    },
                    labelText = "Username",
                    leadingIcon = Icons.Default.Lock,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password
                )
                Spacer(Modifier.height(itemSpacing))
                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }
                Spacer(Modifier.height(itemSpacing))
                Button(
                    onClick = {
                        viewModel.loginEvent(
                            LoginEvents.Login
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
                Spacer(Modifier.height(itemSpacing))
                AlternativeLoginOptions(
                    onIconClick = {
                        scope.launch {
                            val googleIntentSender = viewModel.signInWithGoogleIntentSender()
                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    googleIntentSender ?: return@launch
                                ).build()
                            )
                        }
                    },
                    onSignUpClick,
                    modifier.fillMaxSize()
                        .wrapContentSize(
                            align = Alignment.BottomCenter
                        )
                )
            }
        }
        LoadingView(loginState.isLoading)
    }

}