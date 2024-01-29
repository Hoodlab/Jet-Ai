package hoods.com.jetai.authentication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import hoods.com.jetai.R
import hoods.com.jetai.authentication.register.itemSpacing

@Composable
fun AlternativeLoginOptions(
    onIconClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Or Sign in with")
        Spacer(Modifier.height(itemSpacing))
        OutlinedButton(onClick = onIconClick) {
            Text("Google")
            Spacer(Modifier.height(itemSpacing))
            Icon(
                painter = painterResource(R.drawable.icon_google),
                "login with Google",
                modifier = Modifier
                    .size(32.dp)
            )
        }
        Spacer(Modifier.height(itemSpacing))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an Account?")
            Spacer(Modifier.height(itemSpacing))
            TextButton(onSignUpClick) {
                Text("Sign up")
            }
        }
    }
}