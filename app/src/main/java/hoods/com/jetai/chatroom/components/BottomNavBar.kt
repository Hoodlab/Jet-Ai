package hoods.com.jetai.chatroom.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hoods.com.jetai.navigation.Tabs

@Composable
fun BottomNavBar(
    tabs: List<Tabs>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
) {
    NavigationBar {
        tabs.forEachIndexed { index, tabs ->
            NavigationBarItem(
                alwaysShowLabel = false,
                selected = selectedIndex == index,
                onClick = { onSelectedChange(index) },
                icon = {
                    Icon(imageVector = tabs.icon, contentDescription = tabs.title)
                },
                label = {
                    Text(text = tabs.title)
                }
            )
        }
    }

}