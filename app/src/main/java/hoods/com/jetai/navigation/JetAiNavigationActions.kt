package hoods.com.jetai.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

class JetAiNavigationActions(
    navController: NavController,
) {
    val navigateToForgotPasswordScreen: () -> Unit = {
        navController.navigateToSingleTop(
            Route.ForgotPasswordScreen().route
        )
    }
    val navigateToLoginScreenWithArgs: (isEmailVerified: Boolean) -> Unit = {
        navController.navigate(
            Route.LoginScreen().getRouteWithArgs(isEmailVerified = it)
        ) {
            launchSingleTop = true
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    val navigateToSignUpScreen: () -> Unit = {
        navController.navigate(
            Route.SignupScreen().route
        ) {
            launchSingleTop = true
            popUpTo(Route.LoginScreen().routeWithArgs)
        }
    }

    val navigateToHomeGraph: () -> Unit = {
        navController.navigate(Route.NESTED_HOME_ROUTE) {
            launchSingleTop = true
            popUpTo(Route.LoginScreen().routeWithArgs) { inclusive = true }
            popUpTo(Route.SignupScreen().route) { inclusive = true }
            popUpTo(Route.NESTED_AUTH_ROUTE) { inclusive = true }
        }
    }

    val navigateToVisualIqScreen: () -> Unit = {
        navController.navigate(Tabs.VisualIq.title) {
            launchSingleTop = true
        }
    }


}

fun NavController.navigateToSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}