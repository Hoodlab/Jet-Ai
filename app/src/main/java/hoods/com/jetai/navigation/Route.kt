package hoods.com.jetai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route {
    companion object {
        const val NESTED_AUTH_ROUTE = "auth_route"
        const val NESTED_HOME_ROUTE = "home_route"
        const val isEmailSentArg = "isEmailSent"
    }

    data class LoginScreen(
        val route: String = "login",
        val routeWithArgs: String = "$route/{$isEmailSentArg}",
    ) : Route() {
        fun getRouteWithArgs(isEmailVerified: Boolean = false): String {
            return "$route/$isEmailVerified"
        }
    }

    data class SignupScreen(val route: String = "signup") : Route()
    data class ForgotPasswordScreen(val route: String = "forgotPassword") : Route()
    data class MessageScreen(val route: String = "message") : Route()

}

enum class Tabs(val title: String, val icon: ImageVector) {
    Chats(title = "Chit Chat", icon = Icons.Default.ChatBubble),
    VisualIq(title = "Visual IQ", icon = Icons.Default.PhotoFilter),
}

