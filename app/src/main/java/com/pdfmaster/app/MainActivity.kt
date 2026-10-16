package com.pdfmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.pdfmaster.app.ui.Screen
import com.pdfmaster.app.ui.screens.*
import com.pdfmaster.app.ui.theme.PDFMasterTheme
import com.pdfmaster.app.utils.PDFProcessor
import com.pdfmaster.app.viewmodel.PDFViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PDFProcessor.initialize(this)

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            PDFMasterTheme(darkTheme = isDarkMode) {
                PDFMasterApp(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = it }
                )
            }
        }
    }
}

@Composable
fun PDFMasterApp(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: PDFViewModel = viewModel()

    // Determine if we should show bottom nav
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavRoutes = listOf(Screen.Home.route, Screen.Recent.route, Screen.Settings.route)
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PDFMasterBottomNav(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) 0.dp else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280)) +
                            fadeIn(animationSpec = tween(280))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                            fadeOut(animationSpec = tween(280))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                            fadeIn(animationSpec = tween(280))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280)) +
                            fadeOut(animationSpec = tween(280))
                }
            ) {
                // Bottom Nav Destinations
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(Screen.Recent.route) {
                    RecentScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        navController = navController,
                        viewModel = viewModel,
                        onToggleDarkMode = onToggleDarkMode,
                        isDarkMode = isDarkMode
                    )
                }

                // Organize Tools
                composable(Screen.MergePDF.route) {
                    MergePDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.SplitPDF.route) {
                    SplitPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.CompressPDF.route) {
                    CompressPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.RotatePDF.route) {
                    RotatePDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.RemovePages.route) {
                    RemovePagesScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.ExtractPages.route) {
                    ExtractPagesScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.OrganizePDF.route) {
                    OrganizePDFScreen(navController = navController, viewModel = viewModel)
                }

                // Optimize Tools
                composable(Screen.RepairPDF.route) {
                    RepairPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.OCRPDF.route) {
                    OCRPDFScreen(navController = navController)
                }

                // Convert Tools
                composable(Screen.JPGToPDF.route) {
                    JPGToPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.PDFToJPG.route) {
                    PDFToJPGScreen(navController = navController, viewModel = viewModel)
                }

                // Edit Tools
                composable(Screen.AddWatermark.route) {
                    WatermarkScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.AddPageNumbers.route) {
                    PageNumbersScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.EditPDF.route) {
                    EditPDFScreen(navController = navController)
                }
                composable(Screen.CropPDF.route) {
                    CropPDFScreen(navController = navController)
                }

                // Security Tools
                composable(Screen.ProtectPDF.route) {
                    ProtectPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.UnlockPDF.route) {
                    UnlockPDFScreen(navController = navController, viewModel = viewModel)
                }
                composable(Screen.SignPDF.route) {
                    SignPDFScreen(navController = navController)
                }
                composable(Screen.ComparePDF.route) {
                    ComparePDFScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
fun PDFMasterBottomNav(navController: NavController, currentRoute: String?) {
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        BottomNavItem.items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// Bottom Nav Items with selected/unselected icons
sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Recent : BottomNavItem(Screen.Recent, "Recent", Icons.Filled.History, Icons.Outlined.History)
    object Settings : BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val items = listOf(Home, Recent, Settings)
    }
}
