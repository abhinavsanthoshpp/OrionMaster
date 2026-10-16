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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pdfmaster.app.ui.components.*
import com.pdfmaster.app.ui.theme.ProtectColor
import com.pdfmaster.app.viewmodel.PDFViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectPDFScreen(navController: NavController, viewModel: PDFViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var allowPrinting by remember { mutableStateOf(true) }
    var allowCopying by remember { mutableStateOf(false) }
    val passwordMatch = password == confirmPassword && password.isNotEmpty()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> viewModel.setSelectedUri(uri) }

    Scaffold(
        topBar = { ToolTopBar("Protect PDF", "Password protect your PDF", ProtectColor, Icons.Default.Lock) { navController.popBackStack() } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FileSelector(uiState.selectedUri, context, ProtectColor, { launcher.launch("application/pdf") }) { viewModel.setSelectedUri(null) }

            if (uiState.selectedUri != null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Password Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = confirmPassword, onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = confirmPassword.isNotEmpty() && !passwordMatch,
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        if (confirmPassword.isNotEmpty() && !passwordMatch) {
                            Text("Passwords do not match", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider()
                        Text("Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Allow Printing", style = MaterialTheme.typography.bodyMedium)
                                Text("Users can print the PDF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Switch(checked = allowPrinting, onCheckedChange = { allowPrinting = it }, colors = SwitchDefaults.colors(checkedThumbColor = ProtectColor, checkedTrackColor = ProtectColor.copy(alpha = 0.4f)))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Allow Copying", style = MaterialTheme.typography.bodyMedium)
                                Text("Users can copy text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Switch(checked = allowCopying, onCheckedChange = { allowCopying = it }, colors = SwitchDefaults.colors(checkedThumbColor = ProtectColor, checkedTrackColor = ProtectColor.copy(alpha = 0.4f)))
                        }
                    }
                }
                ProcessButton("Protect PDF", uiState.isLoading, passwordMatch && !uiState.isLoading, ProtectColor) {
                    viewModel.protectPDF(uiState.selectedUri!!, password, allowPrinting, allowCopying)
                }
            } else {
                EmptyStateCard("No file selected", "Select a PDF to password protect it", Icons.Default.Lock, ProtectColor)
            }
            uiState.result?.let { ResultCard(it, context) { viewModel.clearResult() } }
            if (uiState.isLoading) LoadingCard(uiState.statusMessage)
        }
    }
}
