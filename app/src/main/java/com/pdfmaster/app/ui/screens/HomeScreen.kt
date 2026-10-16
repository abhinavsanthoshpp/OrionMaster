package com.pdfmaster.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pdfmaster.app.data.model.*
import com.pdfmaster.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredTools = remember(selectedCategory, searchQuery) {
        PDFTools.getByCategory(selectedCategory).filter {
            searchQuery.isEmpty() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )
        }
    ) { paddingValues ->
        if (filteredTools.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Text("No tools found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("Try a different search term", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group tools by category when viewing ALL
                if (selectedCategory == ToolCategory.ALL && searchQuery.isEmpty()) {
                    ToolCategory.values().filter { it != ToolCategory.ALL }.forEach { cat ->
                        val tools = PDFTools.getByCategory(cat)
                        if (tools.isNotEmpty()) {
                            item(key = "header_$cat") {
                                CategoryHeader(cat)
                            }
                            // Show tools in 2-column grid rows
                            tools.chunked(2).forEachIndexed { idx, pair ->
                                item(key = "row_${cat}_$idx") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        pair.forEach { tool ->
                                            ToolCard(
                                                tool = tool,
                                                modifier = Modifier.weight(1f),
                                                onClick = { navController.navigate(tool.route) }
                                            )
                                        }
                                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            item(key = "spacer_$cat") { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                } else {
                    // Grid view for filtered
                    filteredTools.chunked(2).forEachIndexed { idx, pair ->
                        item(key = "filtered_$idx") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { tool ->
                                    ToolCard(
                                        tool = tool,
                                        modifier = Modifier.weight(1f),
                                        onClick = { navController.navigate(tool.route) }
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: ToolCategory,
    onCategorySelect: (ToolCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "PDF Master",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    "20+ tools • No login required",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
            Icon(
                Icons.Default.PictureAsPdf,
                null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(36.dp)
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search PDF tools…", color = Color.White.copy(alpha = 0.55f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                cursorColor = Color.White
            ),
            singleLine = true
        )

        // Category chips
        ScrollableTabRow(
            selectedTabIndex = ToolCategory.values().indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = {}
        ) {
            ToolCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            category.displayName,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: ToolCategory) {
    val color = getCategoryColor(category)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp, 20.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            category.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ToolCard(
    tool: PDFTool,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val toolColor = Color(tool.colorHex)

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.9f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(toolColor, toolColor.copy(alpha = 0.7f))),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(tool.iconName),
                    contentDescription = tool.name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    tool.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

fun getCategoryColor(category: ToolCategory): Color {
    return when (category) {
        ToolCategory.ALL -> ColorOrganize
        ToolCategory.ORGANIZE -> ColorOrganize
        ToolCategory.OPTIMIZE -> ColorOptimize
        ToolCategory.CONVERT -> ColorConvert
        ToolCategory.EDIT -> ColorEdit
        ToolCategory.SECURITY -> ColorSecurity
    }
}

fun getToolIcon(iconName: String): ImageVector {
    return when (iconName) {
        "merge" -> Icons.Default.MergeType
        "split" -> Icons.Default.CallSplit
        "compress" -> Icons.Default.Compress
        "rotate" -> Icons.Default.RotateRight
        "remove_pages" -> Icons.Default.DeleteOutline
        "extract_pages" -> Icons.Default.FileDownload
        "organize" -> Icons.Default.DragHandle
        "repair" -> Icons.Default.Build
        "ocr" -> Icons.Default.DocumentScanner
        "jpg_to_pdf" -> Icons.Default.Image
        "pdf_to_jpg" -> Icons.Default.PhotoLibrary
        "add_watermark" -> Icons.Default.WaterDrop
        "add_page_numbers" -> Icons.Default.FormatListNumbered
        "crop" -> Icons.Default.Crop
        "edit" -> Icons.Default.Edit
        "protect" -> Icons.Default.Lock
        "unlock" -> Icons.Default.LockOpen
        "sign" -> Icons.Default.Draw
        "compare" -> Icons.Default.CompareArrows
        else -> Icons.Default.PictureAsPdf
    }
}
