package com.quranreader.custom.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranreader.custom.ui.viewmodel.HomeViewModel

/**
 * Modern Home Screen with Resume Reading Banner
 * Uses صفحة (Safha) terminology throughout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReading: (Int) -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownload: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToJuz: () -> Unit = {},
    onNavigateToSessions: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lastPage by viewModel.lastPage.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val sessionTargetPages by viewModel.sessionTargetPages.collectAsState()

    // Resume Reading Banner state
    var showResumeBanner by remember { mutableStateOf(lastPage > 1) }

    var showSessionDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }
    var sessionTargetInput by remember { mutableStateOf("10") }
    var extendPagesInput by remember { mutableStateOf("5") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Quran",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = onNavigateToBookmarks) {
                        Icon(Icons.Default.Bookmark, "Bookmarks")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resume Reading Banner (animated)
            if (showResumeBanner && lastPage > 1) {
                item {
                    ResumeReadingBanner(
                        lastPage = lastPage,
                        onOpen = {
                            showResumeBanner = false
                            onNavigateToReading(lastPage)
                        },
                        onDismiss = { showResumeBanner = false }
                    )
                }
            }

            // Hero: Continue Reading Card
            item {
                ContinueReadingCard(
                    lastPage = lastPage,
                    totalPages = 604,
                    onClick = { onNavigateToReading(lastPage) }
                )
            }

            // Session Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSessionDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Session")
                    }

                    // Extend Session button (only visible when session is active)
                    if (isSessionActive) {
                        OutlinedButton(
                            onClick = { showExtendDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Extend")
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    "Quick Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Juz",
                        icon = Icons.Default.GridView,
                        onClick = onNavigateToJuz,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Bookmarks",
                        icon = Icons.Default.Bookmark,
                        onClick = onNavigateToBookmarks,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Sessions",
                        icon = Icons.Default.Timer,
                        onClick = onNavigateToSessions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Surah List
            item {
                Text(
                    "Surahs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(surahs, key = { it.number }) { surah ->
                SurahCard(
                    surah = surah,
                    onClick = { onNavigateToReading(surah.startPage) }
                )
            }
        }
    }

    // Start Session Dialog
    if (showSessionDialog) {
        AlertDialog(
            onDismissRequest = { showSessionDialog = false },
            title = { Text("Start Reading Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How many صفحة do you want to read?")
                    OutlinedTextField(
                        value = sessionTargetInput,
                        onValueChange = { sessionTargetInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Target صفحة") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = sessionTargetInput.toIntOrNull() ?: 10
                    viewModel.startSession(lastPage, target)
                    showSessionDialog = false
                    onNavigateToReading(lastPage)
                }) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSessionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Extend Session Dialog
    if (showExtendDialog) {
        AlertDialog(
            onDismissRequest = { showExtendDialog = false },
            title = { Text("Extend Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current target: $sessionTargetPages صفحة")
                    Text("Add more صفحة:")
                    OutlinedTextField(
                        value = extendPagesInput,
                        onValueChange = { extendPagesInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Additional صفحة") },
                        singleLine = true
                    )
                    Text(
                        "New target: ${sessionTargetPages + (extendPagesInput.toIntOrNull() ?: 0)} صفحة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val additional = extendPagesInput.toIntOrNull() ?: 5
                    viewModel.extendSession(additional)
                    showExtendDialog = false
                }) {
                    Text("Extend")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Resume Reading Banner - appears at top when user has a saved page
 */
@Composable
private fun ResumeReadingBanner(
    lastPage: Int,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Continue Reading",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "صفحة $lastPage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpen) {
                    Text(
                        "Open",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    lastPage: Int,
    totalPages: Int,
    onClick: () -> Unit
) {
    val progress = lastPage.toFloat() / totalPages.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) // Transparent Aero style
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Continue Reading",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "صفحة $lastPage of $totalPages",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SurahCard(
    surah: com.quranreader.custom.data.model.Surah,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    surah.number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            // Surah info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    surah.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${surah.englishName} • ${surah.ayahCount} verses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Revelation type badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (surah.isMakki) 
                    MaterialTheme.colorScheme.tertiaryContainer
                else 
                    MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    if (surah.isMakki) "Makki" else "Madani",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (surah.isMakki) 
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
