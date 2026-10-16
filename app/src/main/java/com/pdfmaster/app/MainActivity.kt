package com.pdfmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.pdfmaster.app.ui.Screen
import com.pdfmaster.app.ui.screens.*
import com.pdfmaster.app.ui.theme.*
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

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavRoutes = listOf(Screen.Home.route, Screen.Recent.route, Screen.Settings.route)
    val showBottomNav = currentRoute in bottomNavRoutes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(280))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(240))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(280))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(240))
            }
        ) {
            // Main tabs
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

        // Floating Dynamic Island Bottom Dock
        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
        ) {
            FloatingIslandNavBar(navController = navController, currentRoute = currentRoute)
        }
    }
}

// ===================== FLOATING ISLAND DOCK =====================
@Composable
fun FloatingIslandNavBar(navController: NavController, currentRoute: String?) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shadowElevation = 16.dp,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BottomNavItem.items.forEach { item ->
                val selected = currentRoute == item.screen.route
                FloatingNavItem(
                    item = item,
                    isSelected = selected,
                    onClick = {
                        if (currentRoute != item.screen.route) {
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(240),
        label = "nav_bg"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        animationSpec = tween(240),
        label = "nav_tint"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(24.dp),
        color = animatedBg,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = animatedContentColor,
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)),
                exit = shrinkHorizontally(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180))
            ) {
                Row {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryRed
                    )
                }
            }
        }
    }
}

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
