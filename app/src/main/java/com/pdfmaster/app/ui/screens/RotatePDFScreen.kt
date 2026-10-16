package com.pdfmaster.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.RotatePDFColor
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotatePDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var rotation by remember { mutableStateOf(90) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Rotate PDF", "Rotate pages to correct orientation", RotatePDFColor, Icons.Default.RotateRight) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, RotatePDFColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rotation Angle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(90, 180, 270).forEach { angle ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                                            .background(if (rotation == angle) RotatePDFColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                            .border(if (rotation == angle) 2.dp else 1.dp, if (rotation == angle) RotatePDFColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .clickable { rotation = angle },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.RotateRight,
                                            contentDescription = null,
                                            tint = if (rotation == angle) RotatePDFColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(30.dp).rotate((angle - 90).toFloat())
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("${angle}°", style = MaterialTheme.typography.labelMedium, fontWeight = if (rotation == angle) FontWeight.Bold else FontWeight.Normal, color = if (rotation == angle) RotatePDFColor else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
                InfoCard("Rotation will be applied to all pages", RotatePDFColor)
                ProcessButton("Rotate PDF", uiState.isLoading, !uiState.isLoading, RotatePDFColor) {
                    viewModel.rotatePDF(uiState.selectedUri!!, rotation)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to rotate its pages", Icons.Default.RotateRight, RotatePDFColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}
