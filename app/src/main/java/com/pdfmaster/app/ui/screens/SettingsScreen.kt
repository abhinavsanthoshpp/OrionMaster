package com.pdfmaster.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdfmaster.app.viewmodel.PDFViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PDFViewModel = viewModel(),
    onToggleDarkMode: (Boolean) -> Unit,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Appearance
            SettingsSection("Appearance") {
                SettingsSwitchItem(
                    icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconColor = Color(0xFF5C6BC0),
                    title = "Dark Mode",
                    subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }

            // Storage
            SettingsSection("Storage") {
                SettingsClickItem(
                    icon = Icons.Default.CleaningServices,
                    iconColor = Color(0xFF43A047),
                    title = "Clear Cache",
                    subtitle = "Remove temporary files",
                    onClick = { showClearCacheDialog = true }
                )
                SettingsClickItem(
                    icon = Icons.Default.FolderOpen,
                    iconColor = Color(0xFFFF6D00),
                    title = "Output Folder",
                    subtitle = "Downloads/PDFMaster",
                    onClick = { /* Open file manager */ }
                )
            }

            // About
            SettingsSection("About") {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF1E88E5),
                    title = "About PDF Master",
                    subtitle = "Version 1.0 • Open Source",
                    onClick = { showAboutDialog = true }
                )
                SettingsClickItem(
                    icon = Icons.Default.Security,
                    iconColor = Color(0xFF43A047),
                    title = "Privacy",
                    subtitle = "All processing done locally on your device",
                    onClick = { }
                )
                SettingsClickItem(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFCA28),
                    title = "Rate App",
                    subtitle = "If you love PDF Master, give us 5 stars!",
                    onClick = { }
                )
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "100% Private",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your PDF files never leave your device. No internet required for processing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = { Icon(Icons.Default.CleaningServices, null) },
            title = { Text("Clear Cache") },
            text = { Text("This will remove all temporary files. Your processed PDFs in Downloads will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        com.pdfmaster.app.utils.FileUtils.clearCache(context)
                        showClearCacheDialog = false
                    }
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFE53935), modifier = Modifier.size(48.dp)) },
            title = { Text("PDF Master", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 1.0")
                    Text("A free, open-source PDF management app with 20+ tools. No login required. Your files stay on your device.")
                    HorizontalDivider()
                    Text("Built with:", fontWeight = FontWeight.SemiBold)
                    Text("• Jetpack Compose + Material 3")
                    Text("• Apache PDFBox for PDF processing")
                    Text("• Room Database for history")
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column { content() }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
