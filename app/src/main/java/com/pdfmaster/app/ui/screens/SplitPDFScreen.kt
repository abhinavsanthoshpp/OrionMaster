package com.pdfmaster.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.SplitPDFColor
import com.pdfmaster.app.utils.SplitMode
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPDFScreen(
    navController: NavController,
    viewModel: PDFViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMode by remember { mutableStateOf(SplitMode.EachPage) }
    var everyNPages by remember { mutableStateOf("2") }
    var pageRangeText by remember { mutableStateOf("1-3, 4-7") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri -> viewModel.setSelectedUri(uri)
    }

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Split PDF",
                subtitle = "Separate PDF into multiple files",
                color = SplitPDFColor,
                icon = Icons.Default.CallSplit,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(
                uri = uiState.selectedUri,
                context = context,
                color = SplitPDFColor,
                onSelectFile = { launcher.launch("application/pdf") },
                onClear = { viewModel.setSelectedUri(null) }
            )

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Split Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        SplitOption("Split each page", "One PDF per page", selectedMode == SplitMode.EachPage) { selectedMode = SplitMode.EachPage }
                        SplitOption("Every N pages", "Groups of N pages", selectedMode == SplitMode.EveryNPages) { selectedMode = SplitMode.EveryNPages }
                        if (selectedMode == SplitMode.EveryNPages) {
                            OutlinedTextField(
                                value = everyNPages,
                                onValueChange = { everyNPages = it.filter { c -> c.isDigit() } },
                                label = { Text("Pages per group") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                singleLine = true
                            )
                        }
                        SplitOption("Custom ranges", "e.g. 1-3, 4-7", selectedMode == SplitMode.ByRange) { selectedMode = SplitMode.ByRange }
                        if (selectedMode == SplitMode.ByRange) {
                            OutlinedTextField(
                                value = pageRangeText,
                                onValueChange = { pageRangeText = it },
                                label = { Text("Page ranges") },
                                placeholder = { Text("e.g. 1-3, 4-7") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                ProcessButton(
                    text = "Split PDF",
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading,
                    color = SplitPDFColor,
                    onClick = {
                        val uri = uiState.selectedUri!!
                        when (selectedMode) {
                            SplitMode.EachPage -> viewModel.splitPDF(uri, SplitMode.EachPage)
                            SplitMode.EveryNPages -> viewModel.splitPDF(uri, SplitMode.EveryNPages, everyNPages = everyNPages.toIntOrNull() ?: 2)
                            SplitMode.ByRange -> viewModel.splitPDF(uri, SplitMode.ByRange, pageRanges = parsePageRanges(pageRangeText))
                        }
                    }
                )
            } else {
                EmptyStateCard("No file selected", "Select a PDF to split it", Icons.Default.CallSplit, SplitPDFColor)
            }

            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

@Composable
fun SplitOption(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

fun parsePageRanges(text: String): List<IntRange> {
    return text.split(",").mapNotNull { part ->
        val trimmed = part.trim()
        if (trimmed.contains("-")) {
            val p = trimmed.split("-")
            val s = p[0].trim().toIntOrNull()?.minus(1)
            val e = p.getOrNull(1)?.trim()?.toIntOrNull()?.minus(1)
            if (s != null && e != null && s <= e) s..e else null
        } else { val p = trimmed.toIntOrNull()?.minus(1); if (p != null) p..p else null }
    }
}
