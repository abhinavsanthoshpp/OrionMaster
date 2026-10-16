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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.AccentGreen
import com.pdfmaster.app.viewmodel.PDFViewModel

// ===================== REMOVE PAGES SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePagesScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pageInput by remember { mutableStateOf("") }
    val color = Color(0xFFEF5350)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Remove Pages", "Delete specific pages from PDF", color, Icons.Default.DeleteOutline) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pages to Remove", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pageInput, onValueChange = { pageInput = it },
                            label = { Text("Page numbers") },
                            placeholder = { Text("e.g. 1, 3, 5-8") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Enter page numbers separated by commas. Use ranges like 5-8.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                ProcessButton("Remove Pages", uiState.isLoading, pageInput.isNotBlank() && !uiState.isLoading, color) {
                    val pages = parseIndividualPages(pageInput)
                    viewModel.removePages(uiState.selectedUri!!, pages)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to remove pages from", Icons.Default.DeleteOutline, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== EXTRACT PAGES SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractPagesScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pageInput by remember { mutableStateOf("") }
    val color = Color(0xFFF44336)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Extract Pages", "Save specific pages as new PDF", color, Icons.Default.FileDownload) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pages to Extract", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pageInput, onValueChange = { pageInput = it },
                            label = { Text("Page numbers") },
                            placeholder = { Text("e.g. 1, 3, 5-8") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Selected pages will be saved as a new PDF file.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                ProcessButton("Extract Pages", uiState.isLoading, pageInput.isNotBlank() && !uiState.isLoading, color) {
                    viewModel.extractPages(uiState.selectedUri!!, parseIndividualPages(pageInput))
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to extract pages from", Icons.Default.FileDownload, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== ORGANIZE PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizePDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val color = Color(0xFFE53935)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Organize PDF", "Reorder and arrange pages", color, Icons.Default.DragHandle) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Reorder Pages", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Enter new page order as comma-separated numbers.\nExample: 3,1,2 will put page 3 first, then page 1, then page 2.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                InfoCard("Visual drag-and-drop reordering coming in next update!", color)
            } else {
                EmptyStateCard("No file selected", "Select a PDF to organize its pages", Icons.Default.DragHandle, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== REPAIR PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairPDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val color = AccentGreen
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Repair PDF", "Fix corrupted PDF files", color, Icons.Default.Build) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                InfoCard("PDF Master will attempt to rebuild the PDF structure and fix common errors.", color)
                ProcessButton("Repair PDF", uiState.isLoading, !uiState.isLoading, color) {
                    viewModel.compressPDF(uiState.selectedUri!!)
                }
            } else {
                EmptyStateCard("No file selected", "Select a corrupted or damaged PDF to repair", Icons.Default.Build, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== OCR PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRPDFScreen(navController: NavController) {
    val color = Color(0xFF4CAF50)
    Scaffold(
        topBar = { ToolTopBar("OCR PDF", "Make scanned PDFs searchable", color, Icons.Default.DocumentScanner) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.DocumentScanner, null, tint = color, modifier = Modifier.size(72.dp))
                Text("OCR Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Optical Character Recognition will be available in the next update. This feature requires ML Kit integration.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ===================== EDIT PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPDFScreen(navController: NavController) {
    val color = Color(0xFF7B1FA2)
    Scaffold(
        topBar = { ToolTopBar("Edit PDF", "Add text, images and annotations", color, Icons.Default.Edit) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Advanced Editor Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("A full-featured PDF editor with text, image, and annotation tools will be available soon.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ===================== CROP PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPDFScreen(navController: NavController) {
    val color = Color(0xFFAB47BC)
    Scaffold(
        topBar = { ToolTopBar("Crop PDF", "Crop pages to remove margins", color, Icons.Default.Crop) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Crop, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Crop Tool Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Visual crop tool for trimming PDF pages will be available in the next update.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ===================== SIGN PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignPDFScreen(navController: NavController) {
    val color = Color(0xFF42A5F5)
    Scaffold(
        topBar = { ToolTopBar("Sign PDF", "Add digital signature", color, Icons.Default.Draw) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Draw, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Signature Tool Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Draw or type your signature and place it anywhere on your PDF.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ===================== COMPARE PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparePDFScreen(navController: NavController) {
    val color = Color(0xFF26A69A)
    Scaffold(
        topBar = { ToolTopBar("Compare PDF", "Side-by-side comparison", color, Icons.Default.CompareArrows) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.CompareArrows, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Compare Tool Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Compare two PDF files side by side and highlight differences.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ===================== HELPER FUNCTIONS =====================
fun parseIndividualPages(text: String): List<Int> {
    val pages = mutableListOf<Int>()
    text.split(",").forEach { part ->
        val trimmed = part.trim()
        if (trimmed.contains("-")) {
            val parts = trimmed.split("-")
            val start = parts[0].trim().toIntOrNull()?.minus(1)
            val end = parts.getOrNull(1)?.trim()?.toIntOrNull()?.minus(1)
            if (start != null && end != null && start <= end) {
                pages.addAll(start..end)
            }
        } else {
            trimmed.toIntOrNull()?.minus(1)?.let { pages.add(it) }
        }
    }
    return pages.distinct().sorted()
}
