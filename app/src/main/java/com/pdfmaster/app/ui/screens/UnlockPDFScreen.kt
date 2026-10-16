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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.UnlockColor
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockPDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Unlock PDF", "Remove password protection", UnlockColor, Icons.Default.LockOpen) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, UnlockColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }
            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Enter Password", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text("PDF Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    }
                }
                InfoCard("The unlocked PDF will be saved without any password", UnlockColor)
                ProcessButton("Unlock PDF", uiState.isLoading, password.isNotEmpty() && !uiState.isLoading, UnlockColor) {
                    viewModel.unlockPDF(uiState.selectedUri!!, password)
                }
            } else {
                EmptyStateCard("No file selected", "Select a password-protected PDF to unlock", Icons.Default.LockOpen, UnlockColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}
