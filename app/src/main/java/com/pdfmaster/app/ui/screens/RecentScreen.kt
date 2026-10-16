package com.pdfmaster.app.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.data.local.RecentFileEntity
import com.pdfmaster.app.ui.components.openFile
import com.pdfmaster.app.ui.components.shareFile
import com.pdfmaster.app.ui.theme.*
import com.pdfmaster.app.viewmodel.PDFViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    navController: NavController,
    viewModel: PDFViewModel = viewModel()
) {
    val context = LocalContext.current
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Recent History",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${recentFiles.size} processed documents",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                        )
                    }

                    if (recentFiles.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (recentFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    Text(
                        "Vault is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "Documents you merge, compress, protect, or convert will appear here for fast access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentFiles, key = { it.id }) { file ->
                    RecentFileItem(
                        file = file,
                        onOpen = { openFile(context, file.path) },
                        onShare = { shareFile(context, file.path) },
                        onDelete = { viewModel.deleteRecentFile(file.id) }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Vault History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all recent entries from the app history. Your files in Downloads will remain safe on your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecent()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun RecentFileItem(
    file: RecentFileEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val fileExists = remember(file.path) { File(file.path).exists() }
    val dateStr = remember(file.lastOpened) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(file.lastOpened))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_scale"
    )

    Surface(
        onClick = onOpen,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatSize(file.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Text(
                        dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                file.toolUsed?.let { toolName ->
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = PrimaryRed.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            toolName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        else -> String.format("%.1fMB", bytes / (1024f * 1024f))
    }
}
