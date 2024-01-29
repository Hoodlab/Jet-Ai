package hoods.com.jetai.authentication.forgot_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hoods.com.jetai.authentication.components.LoginTextField
import hoods.com.jetai.authentication.register.defaultPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(),
    onBackClick: () -> Unit,
) {
    val state = forgotPasswordViewModel.forgotPasswordState
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(Icons.Default.ArrowBack, "navigate Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(defaultPadding).padding(innerPadding)
        ) {
            AnimatedVisibility(state.errorMsg != null) {
                Text(
                    state.errorMsg ?: "Unknown",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LoginTextField(
                value = state.email,
                onValueChange = {
                    forgotPasswordViewModel.forgotPasswordEvent(
                        ForgotPasswordEvent.OnEmailChange(it)
                    )
                },
                labelText = "Email",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email
            )
            Text(
                "Your confirmation link will be sent to your email address",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = .5f
                )
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    forgotPasswordViewModel.forgotPasswordEvent(
                        ForgotPasswordEvent.SendForgotPasswordLink
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send")
            }
        }
    }

}