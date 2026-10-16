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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.CompressPDFColor
import com.pdfmaster.app.utils.CompressionQuality
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressPDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedQuality by remember { mutableStateOf(CompressionQuality.MEDIUM) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Compress PDF", "Reduce file size", CompressPDFColor, Icons.Default.Compress) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, CompressPDFColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Compression Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                Triple("Low", "Best quality", CompressionQuality.LOW),
                                Triple("Medium", "Balanced", CompressionQuality.MEDIUM),
                                Triple("High", "Smallest", CompressionQuality.HIGH)
                            ).forEach { (label, desc, quality) ->
                                CompressOption(label, desc, quality, selectedQuality == quality, CompressPDFColor, { selectedQuality = quality }, Modifier.weight(1f))
                            }
                        }
                    }
                }
                InfoCard("Output saved to Downloads/PDFMaster", CompressPDFColor)
                ProcessButton("Compress PDF", uiState.isLoading, !uiState.isLoading, CompressPDFColor) {
                    viewModel.compressPDF(uiState.selectedUri!!, selectedQuality)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to reduce its size", Icons.Default.Compress, CompressPDFColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

@Composable
fun CompressOption(label: String, desc: String, quality: CompressionQuality, isSelected: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}
