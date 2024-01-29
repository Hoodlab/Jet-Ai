package hoods.com.jetai.navigation

import android.util.Log
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import hoods.com.jetai.authentication.forgot_password.ForgotPasswordScreen
import hoods.com.jetai.authentication.login.LoginScreen
import hoods.com.jetai.authentication.login.LoginViewModel
import hoods.com.jetai.authentication.login.LoginViewModel.Companion.TAG
import hoods.com.jetai.authentication.register.SignUpScreen
import hoods.com.jetai.chatroom.ChatRoomScreen
import hoods.com.jetai.chatroom.ChatRoomViewModel
import hoods.com.jetai.chatroom.EMPTY_TITLE
import hoods.com.jetai.message.MessageScreen
import hoods.com.jetai.message.MessageViewModel
import hoods.com.jetai.message.MessageViewModelFactory
import hoods.com.jetai.photo_reasoning.PhotoReasoningScreen

@Composable
fun JetAiNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navAction: JetAiNavigationActions,
    loginViewModel: LoginViewModel,
    chatRoomViewModel: ChatRoomViewModel,
    startDestination: String,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        Log.i(TAG, "JetAiNavGraph: $startDestination")
        authGraph(
            navAction, navController, loginViewModel, modifier
        )
        homeGraph(navAction, navController, chatRoomViewModel, modifier)

    }

}

fun NavGraphBuilder.authGraph(
    navAction: JetAiNavigationActions,
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    modifier: Modifier,
) {
    navigation(
        startDestination = Route.LoginScreen().routeWithArgs,
        route = Route.NESTED_AUTH_ROUTE,
    ) {
        composable(
            route = Route.LoginScreen().routeWithArgs,
            arguments = listOf(
                navArgument(name = Route.isEmailSentArg) {}
            )
        ) { entry ->
            LoginScreen(
                onSignUpClick = {
                    navAction.navigateToSignUpScreen()
                },
                isVerificationEmailSent = entry.arguments?.getString(Route.isEmailSentArg)
                    .toBoolean(),
                onForgotPasswordClick = {
                    navAction.navigateToForgotPasswordScreen()
                },
                navigateToHomeScreen = {
                    navAction.navigateToHomeGraph()
                },
                modifier = modifier,
                viewModel = loginViewModel
            )
        }
        composable(route = Route.SignupScreen().route) {
            SignUpScreen(
                onLoginClick = {
                    navAction.navigateToLoginScreenWithArgs(false)
                },
                onNavigateToLoginScreen = {
                    navAction.navigateToLoginScreenWithArgs(it)
                },
                onBackButtonClicked = {
                    navAction.navigateToLoginScreenWithArgs(false)
                },
                modifier = modifier
            )
        }
        composable(route = Route.ForgotPasswordScreen().route) {
            ForgotPasswordScreen {
                navController.navigateUp()
            }
        }
    }
}

fun NavGraphBuilder.homeGraph(
    navAction: JetAiNavigationActions,
    navController: NavHostController,
    chatRoomViewModel: ChatRoomViewModel,
    modifier: Modifier = Modifier,
) {
    val messageRoute = "${Route.MessageScreen().route}/{chatId}/{chatTitle}"

    navigation(startDestination = Tabs.Chats.title, route = Route.NESTED_HOME_ROUTE) {
        composable(route = Tabs.Chats.title) {
            ChatRoomScreen(
                modifier,
                chatRoomViewModel
            ) { id, chatTitle ->
                navController.navigate("${Route.MessageScreen().route}/$id/$chatTitle") {
                    launchSingleTop = true
                    popUpTo(Route.MessageScreen().route) { saveState = true }
                    restoreState = true
                }
            }
        }
        composable(
            route = messageRoute,
            arguments = listOf(navArgument("chatId") {}, navArgument("chatTitle") {})
        ) { backStack ->
            val id = backStack.arguments?.getString("chatId") ?: ""
            val chatTitle = backStack.arguments?.getString("chatTitle") ?: EMPTY_TITLE
            val viewModel: MessageViewModel =
                viewModel(factory = MessageViewModelFactory(id, chatTitle))
            MessageScreen(
                modifier = modifier,
                messageViewModel = viewModel
            )
        }

        composable(
            route = Tabs.VisualIq.title,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            PhotoReasoningScreen(modifier = modifier)
        }

    }
}