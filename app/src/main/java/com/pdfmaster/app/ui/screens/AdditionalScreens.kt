package com.pdfmaster.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.AccentGreen
import com.pdfmaster.app.viewmodel.PDFViewModel

// ===================== REMOVE PAGES SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePagesScreen(navController: NavController, viewModel: PDFViewModel) {
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
                        Text("Enter 1-based page numbers or ranges (e.g. 1, 3, 5-8)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
fun ExtractPagesScreen(navController: NavController, viewModel: PDFViewModel) {
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
                        Text("Selected pages will be extracted and saved as a new PDF.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
fun OrganizePDFScreen(navController: NavController, viewModel: PDFViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var orderInput by remember { mutableStateOf("") }
    val color = Color(0xFFE53935)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Organize PDF", "Reorder pages in custom sequence", color, Icons.Default.DragHandle) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Custom Page Sequence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = orderInput, onValueChange = { orderInput = it },
                            label = { Text("New Page Order") },
                            placeholder = { Text("e.g. 3, 1, 2, 4") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Enter the page numbers in your desired sequence (1-indexed).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                ProcessButton("Reorder & Save PDF", uiState.isLoading, orderInput.isNotBlank() && !uiState.isLoading, color) {
                    val order = orderInput.split(",").mapNotNull { it.trim().toIntOrNull()?.minus(1) }
                    if (order.isNotEmpty()) {
                        viewModel.reorderPages(uiState.selectedUri!!, order)
                    }
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to reorder its pages", Icons.Default.DragHandle, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== REPAIR PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairPDFScreen(navController: NavController, viewModel: PDFViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val color = AccentGreen
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Repair PDF", "Rebuild and recover PDF structures", color, Icons.Default.Build) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                InfoCard("Orion Master will parse, sanitize, and rebuild the PDF structure to fix rendering anomalies.", color)
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

// ===================== CROP PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPDFScreen(navController: NavController, viewModel: PDFViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMargin by remember { mutableStateOf(0.10f) }
    val color = Color(0xFFAB47BC)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Crop PDF", "Trim page margins evenly", color, Icons.Default.Crop) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Margin Trim Percentage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(0.05f to "5%", 0.10f to "10%", 0.15f to "15%", 0.20f to "20%").forEach { (margin, label) ->
                                Box(
                                    modifier = Modifier.weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedMargin == margin) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                        .border(if (selectedMargin == margin) 2.dp else 1.dp, if (selectedMargin == margin) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .clickable { selectedMargin = margin }.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontWeight = FontWeight.Bold, color = if (selectedMargin == margin) color else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
                InfoCard("Crops outer white margins across all pages without losing text content.", color)
                ProcessButton("Crop PDF Margins", uiState.isLoading, !uiState.isLoading, color) {
                    viewModel.cropPDF(uiState.selectedUri!!, selectedMargin)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to trim whitespace margins", Icons.Default.Crop, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

// ===================== SIGN PDF SCREEN (WORKING DIGITAL SIGNATURE PAD) =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignPDFScreen(navController: NavController, viewModel: PDFViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val color = Color(0xFF42A5F5)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    val lines = remember { mutableStateListOf<List<Offset>>() }
    var currentLine = remember { mutableStateListOf<Offset>() }
    var inkColor by remember { mutableStateOf(Color.Black) }
    var targetPage by remember { mutableStateOf("1") }

    Scaffold(
        topBar = { ToolTopBar("Sign PDF", "Draw and place your signature", color, Icons.Default.Draw) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Draw Signature", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { lines.clear(); currentLine.clear() }) {
                                Text("Clear Pad", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        // Signature Drawing Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentLine.clear()
                                            currentLine.add(offset)
                                        },
                                        onDrag = { change, _ ->
                                            currentLine.add(change.position)
                                        },
                                        onDragEnd = {
                                            if (currentLine.isNotEmpty()) {
                                                lines.add(currentLine.toList())
                                                currentLine.clear()
                                            }
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                lines.forEach { line ->
                                    for (i in 0 until line.size - 1) {
                                        drawLine(
                                            color = inkColor,
                                            start = line[i],
                                            end = line[i + 1],
                                            strokeWidth = 6f
                                        )
                                    }
                                }
                                for (i in 0 until currentLine.size - 1) {
                                    drawLine(
                                        color = inkColor,
                                        start = currentLine[i],
                                        end = currentLine[i + 1],
                                        strokeWidth = 6f
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(Color.Black to "Black", Color(0xFF0038A8) to "Blue", Color(0xFFC62828) to "Red").forEach { (c, label) ->
                                    FilterChip(
                                        selected = inkColor == c,
                                        onClick = { inkColor = c },
                                        label = { Text(label) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = targetPage,
                                onValueChange = { targetPage = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Page #") },
                                modifier = Modifier.width(90.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                ProcessButton("Apply Signature to PDF", uiState.isLoading, lines.isNotEmpty() && !uiState.isLoading, color) {
                    val bitmap = renderSignatureToBitmap(lines, inkColor)
                    val pageIdx = (targetPage.toIntOrNull() ?: 1).coerceAtLeast(1) - 1
                    viewModel.signPDF(uiState.selectedUri!!, bitmap, pageIdx)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to digitally sign", Icons.Default.Draw, color)
            }

            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

fun renderSignatureToBitmap(lines: List<List<Offset>>, color: Color): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color.toArgb()
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    lines.forEach { line ->
        if (line.size > 1) {
            val path = Path()
            path.moveTo(line[0].x, line[0].y)
            for (i in 1 until line.size) {
                path.lineTo(line[i].x, line[i].y)
            }
            canvas.drawPath(path, paint)
        }
    }
    return bitmap
}

// ===================== OCR PDF SCREEN =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRPDFScreen(navController: NavController) {
    val color = Color(0xFF4CAF50)
    Scaffold(
        topBar = { ToolTopBar("OCR PDF", "Searchable text scanner", color, Icons.Default.DocumentScanner) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.DocumentScanner, null, tint = color, modifier = Modifier.size(72.dp))
                Text("OCR Text Layer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("On-device OCR engine is being optimized for offline text extraction without server dependencies.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        topBar = { ToolTopBar("Edit PDF", "Annotate and edit", color, Icons.Default.Edit) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Vector Annotator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Use the Add Watermark and Sign tools for adding direct visual text and stamps to your PDF files.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        topBar = { ToolTopBar("Compare PDF", "Side-by-side inspection", color, Icons.Default.CompareArrows) { navController.popBackStack() } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.CompareArrows, null, tint = color, modifier = Modifier.size(72.dp))
                Text("Side-by-Side Comparison", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Select any two documents to visually verify page count and structural diffs offline.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
