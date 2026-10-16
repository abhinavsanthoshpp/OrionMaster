package com.pdfmaster.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pdfmaster.app.ui.theme.MergePDFColor
import com.pdfmaster.app.utils.FileUtils
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePDFScreen(
    navController: NavController,
    viewModel: PDFViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedUris = selectedUris + uris }

    LaunchedEffect(uiState.result) {
        if (uiState.result?.success == true) selectedUris = emptyList()
    }

    Scaffold(
        topBar = {
            ToolTopBar(
                title = "Merge PDF",
                subtitle = "Combine multiple PDFs into one",
                color = MergePDFColor,
                icon = Icons.Default.MergeType,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AddFileButton(
                text = "Add PDF Files",
                color = MergePDFColor,
                onClick = { launcher.launch("application/pdf") }
            )

            if (selectedUris.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${selectedUris.size} files selected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { selectedUris = emptyList() }) {
                                Text("Clear All")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(selectedUris) { index, uri ->
                                FileListItem(
                                    index = index + 1,
                                    name = FileUtils.getFileName(context, uri) + ".pdf",
                                    size = FileUtils.formatFileSize(FileUtils.getFileSize(context, uri)),
                                    color = MergePDFColor,
                                    onRemove = {
                                        selectedUris = selectedUris.toMutableList().also { it.removeAt(index) }
                                    }
                                )
                            }
                        }
                    }
                }

                InfoCard(
                    text = "Files will be merged in the order shown. Add more files to continue.",
                    color = MergePDFColor
                )

                ProcessButton(
                    text = "Merge ${selectedUris.size} PDFs",
                    isLoading = uiState.isLoading,
                    enabled = selectedUris.size >= 2 && !uiState.isLoading,
                    color = MergePDFColor,
                    onClick = { viewModel.mergePDFs(selectedUris) }
                )
            } else {
                EmptyStateCard(
                    title = "No files selected",
                    description = "Add at least 2 PDF files to merge them into one",
                    icon = Icons.Default.MergeType,
                    color = MergePDFColor
                )
            }

            uiState.result?.let {
                ResultCard(result = it, context = context) { viewModel.clearResult() }
            }
            if (uiState.isLoading) LoadingCard(message = uiState.statusMessage)
        }
    }
}
