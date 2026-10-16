package com.pdfmaster.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pdfmaster.app.data.model.*
import com.pdfmaster.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedCategory by rememberSaveable { mutableStateOf(ToolCategory.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val gridState = rememberLazyGridState()

    val filteredTools = remember(selectedCategory, searchQuery) {
        val baseList = PDFTools.getByCategory(selectedCategory)
        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.name.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.category.displayName.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Show Featured Bento Section only when on ALL tab and no search
            if (selectedCategory == ToolCategory.ALL && searchQuery.isEmpty()) {
                item(span = { GridItemSpan(2) }, key = "featured_header") {
                    SectionHeader(
                        title = "Essential Tools",
                        subtitle = "Most used PDF actions"
                    )
                }

                // 2 Large Featured Bento Cards
                items(PDFTools.featuredTools, key = { "featured_${it.id}" }, span = { GridItemSpan(1) }) { tool ->
                    FeaturedBentoCard(
                        tool = tool,
                        onClick = { navController.navigate(tool.route) }
                    )
                }

                item(span = { GridItemSpan(2) }, key = "all_tools_header") {
                    Spacer(Modifier.height(8.dp))
                    SectionHeader(
                        title = "All Utilities",
                        subtitle = "100% on-device • Instant processing"
                    )
                }
            }

            // Normal Tools Grid
            if (filteredTools.isEmpty()) {
                item(span = { GridItemSpan(2) }, key = "empty_search") {
                    EmptySearchPlaceholder(searchQuery = searchQuery)
                }
            } else {
                items(filteredTools, key = { it.id }) { tool ->
                    ModernToolCard(
                        tool = tool,
                        onClick = { navController.navigate(tool.route) }
                    )
                }
            }
        }
    }
}

// ===================== HEADER & SEARCH =====================
@Composable
fun HomeHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: ToolCategory,
    onSelectCategory: (ToolCategory) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            // Top App Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryRed
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Master",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Private • Local Vault • Zero Cloud",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Security Shield Badge
                Surface(
                    color = AccentGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Text(
                            "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Search 20+ PDF tools…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Smooth Horizontal Category Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ToolCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    CategoryFilterChip(
                        category = category,
                        isSelected = isSelected,
                        onClick = { onSelectCategory(category) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ===================== CATEGORY CHIP =====================
@Composable
fun CategoryFilterChip(
    category: ToolCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "chip_bg"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        label = "chip_text"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        color = animatedColor,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Text(
            text = category.displayName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = animatedTextColor
        )
    }
}

// ===================== SECTION HEADER =====================
@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

// ===================== FEATURED BENTO CARD =====================
@Composable
fun FeaturedBentoCard(
    tool: PDFTool,
    onClick: () -> Unit
) {
    val toolColor = Color(tool.colorHex)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bento_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, toolColor.copy(alpha = 0.25f)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(toolColor, toolColor.copy(alpha = 0.75f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getToolIcon(tool.iconName),
                        contentDescription = tool.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                tool.badge?.let { badge ->
                    Surface(
                        color = toolColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = toolColor
                        )
                    }
                }
            }

            Column {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ===================== MODERN REGULAR TOOL CARD =====================
@Composable
fun ModernToolCard(
    tool: PDFTool,
    onClick: () -> Unit
) {
    val toolColor = Color(tool.colorHex)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(toolColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(tool.iconName),
                    contentDescription = tool.name,
                    tint = toolColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// ===================== EMPTY PLACEHOLDER =====================
@Composable
fun EmptySearchPlaceholder(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "No tools matching \"$searchQuery\"",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "Check spelling or explore by category tabs above",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ===================== TOOL ICON RESOLVER =====================
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
