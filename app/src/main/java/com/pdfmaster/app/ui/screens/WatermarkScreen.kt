package com.pdfmaster.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.WatermarkColor
import com.pdfmaster.app.utils.WatermarkColor as WCColor
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var opacity by remember { mutableStateOf(0.3f) }
    var fontSize by remember { mutableStateOf(48f) }
    var selectedColor by remember { mutableStateOf(WCColor.GRAY) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Add Watermark", "Stamp text onto PDF pages", WatermarkColor, Icons.Default.WaterDrop) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, WatermarkColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Watermark Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = watermarkText, onValueChange = { watermarkText = it },
                            label = { Text("Watermark Text") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Text("Opacity: ${(opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.1f..0.9f, colors = SliderDefaults.colors(thumbColor = WatermarkColor, activeTrackColor = WatermarkColor))
                        Text("Font Size: ${fontSize.toInt()}pt", style = MaterialTheme.typography.bodySmall)
                        Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 20f..100f, colors = SliderDefaults.colors(thumbColor = WatermarkColor, activeTrackColor = WatermarkColor))
                        Text("Color", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(WCColor.GRAY to "Gray", WCColor.BLACK to "Black", WCColor.RED to "Red", WCColor.BLUE to "Blue").forEach { (color, label) ->
                                FilterChip(
                                    selected = selectedColor == color,
                                    onClick = { selectedColor = color },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WatermarkColor.copy(alpha = 0.2f), selectedLabelColor = WatermarkColor)
                                )
                            }
                        }
                    }
                }
                ProcessButton("Add Watermark", uiState.isLoading, watermarkText.isNotBlank() && !uiState.isLoading, WatermarkColor) {
                    viewModel.addWatermark(uiState.selectedUri!!, watermarkText, opacity, 45f, fontSize, selectedColor)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to add a watermark", Icons.Default.WaterDrop, WatermarkColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}
