package com.example.ui.components
 
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun MainLayout(
    viewModel: MainViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom bar on all screens except pin setup and login
            if (activeScreen != "setup_pin" && activeScreen != "login") {
                val outlineColor = MaterialTheme.colorScheme.outline
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(64.dp)
                        .drawBehind {
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeScreen == "dashboard",
                        onClick = { viewModel.navigateTo("dashboard") },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Panel") },
                        label = { Text("Panel", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color(0xFF6E6E73),
                            unselectedTextColor = Color(0xFF6E6E73),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )
 
                    NavigationBarItem(
                        selected = activeScreen == "history",
                        onClick = { viewModel.navigateTo("history") },
                        icon = { Icon(Icons.Default.List, contentDescription = "Historial") },
                        label = { Text("Historial", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color(0xFF6E6E73),
                            unselectedTextColor = Color(0xFF6E6E73),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_history")
                    )
 
                    NavigationBarItem(
                        selected = activeScreen == "purchase_form",
                        onClick = { 
                            viewModel.setEditingPurchase(null) // Clear any editing state for a new form
                            viewModel.navigateTo("purchase_form") 
                        },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Registrar") },
                        label = { Text("Registrar", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color(0xFF6E6E73),
                            unselectedTextColor = Color(0xFF6E6E73),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_add")
                    )
 
                    NavigationBarItem(
                        selected = activeScreen == "settings",
                        onClick = { viewModel.navigateTo("settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                        label = { Text("Ajustes", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color(0xFF6E6E73),
                            unselectedTextColor = Color(0xFF6E6E73),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content(Modifier)
        }
    }
}
