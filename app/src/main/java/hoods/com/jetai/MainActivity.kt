package hoods.com.jetai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hoods.com.jetai.authentication.login.LoginEvents
import hoods.com.jetai.authentication.login.LoginScreen
import hoods.com.jetai.authentication.login.LoginViewModel
import hoods.com.jetai.authentication.register.SignUpScreen
import hoods.com.jetai.chatroom.ChatRoomViewModel
import hoods.com.jetai.chatroom.components.BottomNavBar
import hoods.com.jetai.navigation.JetAiNavGraph
import hoods.com.jetai.navigation.JetAiNavigationActions
import hoods.com.jetai.navigation.Route
import hoods.com.jetai.navigation.Tabs
import hoods.com.jetai.ui.theme.JetAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetAiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JetAiApp()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun JetAiApp() {
        val navController = rememberNavController()
        val navAction = remember {
            JetAiNavigationActions(navController)
        }
        val loginViewModel: LoginViewModel = viewModel()
        val authState by remember { mutableStateOf(loginViewModel.hasUserVerified()) }
        val startRoute = if (authState) Route.NESTED_HOME_ROUTE else Route.NESTED_AUTH_ROUTE
        val stateDestination by navController.currentBackStackEntryAsState()
        val chatRoomViewModel: ChatRoomViewModel = viewModel()
        val currentDestination = stateDestination?.destination?.route
        val isChatRoomDestination = currentDestination == Tabs.Chats.title
        val isHomeDestination =
            currentDestination == Route.NESTED_HOME_ROUTE
                    || currentDestination == Tabs.Chats.title
                    || currentDestination == Tabs.VisualIq.title
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = Tabs.entries.toList()

        LaunchedEffect(stateDestination) {
            when (currentDestination) {
                Tabs.Chats.title -> {
                    selectedTabIndex = 0
                }

                Tabs.VisualIq.title -> {
                    selectedTabIndex = 1
                }
            }
        }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(isHomeDestination) {
                    BottomNavBar(
                        tabs = tabs,
                        selectedIndex = selectedTabIndex,
                        onSelectedChange = {
                            selectedTabIndex = it
                            when (it) {
                                0 -> {
                                    navAction.navigateToHomeGraph()
                                }

                                1 -> {
                                    navAction.navigateToVisualIqScreen()
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                AnimatedVisibility(isChatRoomDestination) {
                    SmallFloatingActionButton(
                        onClick = {
                            chatRoomViewModel.newChatRoom()
                        }
                    ) {
                        Icon(painter = painterResource(R.mipmap.ic_jet_ai), "new chat")
                    }
                }
            },
            topBar = {
                AnimatedVisibility(isHomeDestination) {
                    TopAppBar(
                        title = { Text("JetAi") },
                        actions = {
                            IconButton(
                                onClick = {
                                    loginViewModel.loginEvent(
                                        LoginEvents.LogOut
                                    )
                                    navController.navigate(
                                        Route.LoginScreen().getRouteWithArgs(false)
                                    ) {
                                        popUpTo(Tabs.Chats.title) { inclusive = true }
                                        popUpTo(Tabs.VisualIq.title) { inclusive = true }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Logout, "Log out")
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            JetAiNavGraph(
                navController = navController,
                navAction = navAction,
                loginViewModel = loginViewModel,
                chatRoomViewModel = chatRoomViewModel,
                startDestination = startRoute,
                modifier = Modifier.padding(innerPadding)
            )
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetAiTheme {
        Greeting("Android")
    }
}