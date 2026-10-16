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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.PDFtoJPGColor
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFToJPGScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDpi by remember { mutableStateOf(150) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("PDF to JPG", "Convert pages to images", PDFtoJPGColor, Icons.Default.PhotoLibrary) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, PDFtoJPGColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Image Quality (DPI)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(72 to "Low", 150 to "Medium", 300 to "High").forEach { (dpi, label) ->
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedDpi == dpi) PDFtoJPGColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                        .border(if (selectedDpi == dpi) 2.dp else 1.dp, if (selectedDpi == dpi) PDFtoJPGColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .clickable { selectedDpi = dpi }.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (selectedDpi == dpi) PDFtoJPGColor else MaterialTheme.colorScheme.onSurface)
                                        Text("${dpi} DPI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
                InfoCard("Each page will be saved as a separate JPG image", PDFtoJPGColor)
                ProcessButton("Convert to JPG", uiState.isLoading, !uiState.isLoading, PDFtoJPGColor) {
                    viewModel.pdfToJpg(uiState.selectedUri!!, selectedDpi)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to convert its pages to JPG images", Icons.Default.PhotoLibrary, PDFtoJPGColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}
