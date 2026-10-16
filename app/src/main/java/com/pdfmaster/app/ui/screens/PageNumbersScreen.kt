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
import com.pdfmaster.app.ui.theme.AccentPurple
import com.pdfmaster.app.utils.PageNumberPosition
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageNumbersScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPosition by remember { mutableStateOf(PageNumberPosition.BOTTOM_CENTER) }
    var startFrom by remember { mutableStateOf("1") }
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    val color = AccentPurple
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Page Numbers", "Add numbering to PDF", color, Icons.Default.FormatListNumbered) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, color, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Number Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = startFrom,
                            onValueChange = { startFrom = it.filter { c -> c.isDigit() } },
                            label = { Text("Start from") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = prefix,
                                onValueChange = { prefix = it },
                                label = { Text("Prefix") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("e.g. Page ") }
                            )
                            OutlinedTextField(
                                value = suffix,
                                onValueChange = { suffix = it },
                                label = { Text("Suffix") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("e.g. /10") }
                            )
                        }
                        Text("Position", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        PagePositionGrid(selectedPosition, color) { selectedPosition = it }
                    }
                }
                ProcessButton("Add Page Numbers", uiState.isLoading, !uiState.isLoading, color) {
                    viewModel.addPageNumbers(uiState.selectedUri!!, selectedPosition, startFrom.toIntOrNull() ?: 1, prefix, suffix)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to add page numbers", Icons.Default.FormatListNumbered, color)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}

@Composable
fun PagePositionGrid(
    selected: PageNumberPosition,
    color: Color,
    onSelect: (PageNumberPosition) -> Unit
) {
    val positions = listOf(
        PageNumberPosition.TOP_LEFT to "Top Left",
        PageNumberPosition.TOP_CENTER to "Top Center",
        PageNumberPosition.TOP_RIGHT to "Top Right",
        PageNumberPosition.BOTTOM_LEFT to "Bottom Left",
        PageNumberPosition.BOTTOM_CENTER to "Bottom Center",
        PageNumberPosition.BOTTOM_RIGHT to "Bottom Right"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        positions.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (position, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected == position) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                            .border(
                                if (selected == position) 2.dp else 1.dp,
                                if (selected == position) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelect(position) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected == position) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected == position) color else MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
