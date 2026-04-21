package com.quranreader.custom.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranreader.custom.R
import com.quranreader.custom.ui.MainActivity
import com.quranreader.custom.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen — NO TopAppBar
 * - Display Theme: 4 palettes + Material You
 * - Pages per session: numeric input
 * - Language, Downloads, Clear bookmarks, About
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToDownload: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeId by viewModel.themeId.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    var showClearBookmarksDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var pendingLanguageCode by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Section: Display Theme ───────────────────────────────────────────
        item {
            Text(
                context.getString(R.string.settings_display_theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Zamrud Islami
                    ThemeOptionRow(
                        name = context.getString(R.string.settings_theme_zamrud),
                        lightId = "zamrud_light",
                        darkId = "zamrud_dark",
                        primaryLightColor = Color(0xFF1B6B45),
                        bgLightColor = Color(0xFFF8F5F0),
                        primaryDarkColor = Color(0xFF78D9A5),
                        bgDarkColor = Color(0xFF131612),
                        currentThemeId = themeId,
                        onSelect = { viewModel.setThemeId(it) }
                    )

                    Divider()

                    // Teal & Dusk
                    ThemeOptionRow(
                        name = context.getString(R.string.settings_theme_teal),
                        lightId = "teal_light",
                        darkId = "teal_dark",
                        primaryLightColor = Color(0xFF1A5F8D),
                        bgLightColor = Color(0xFFF3F7FB),
                        primaryDarkColor = Color(0xFF7AC8E8),
                        bgDarkColor = Color(0xFF0D1318),
                        currentThemeId = themeId,
                        onSelect = { viewModel.setThemeId(it) }
                    )

                    Divider()

                    // Amber Masjid
                    ThemeOptionRow(
                        name = context.getString(R.string.settings_theme_amber),
                        lightId = "amber_light",
                        darkId = "amber_dark",
                        primaryLightColor = Color(0xFF8B5E0A),
                        bgLightColor = Color(0xFFFBF8F2),
                        primaryDarkColor = Color(0xFFFAC775),
                        bgDarkColor = Color(0xFF17120A),
                        currentThemeId = themeId,
                        onSelect = { viewModel.setThemeId(it) }
                    )

                    Divider()

                    // Indigo Malam
                    ThemeOptionRow(
                        name = context.getString(R.string.settings_theme_indigo),
                        lightId = "indigo_light",
                        darkId = "indigo_dark",
                        primaryLightColor = Color(0xFF303083),
                        bgLightColor = Color(0xFFF5F4FC),
                        primaryDarkColor = Color(0xFFB0ACFF),
                        bgDarkColor = Color(0xFF0E0D18),
                        currentThemeId = themeId,
                        onSelect = { viewModel.setThemeId(it) }
                    )

                    // Material You — only visible on API 31+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Divider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setThemeId("material_you") }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        context.getString(R.string.settings_theme_material_you),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        context.getString(R.string.settings_theme_material_you_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            RadioButton(
                                selected = themeId == "material_you",
                                onClick = { viewModel.setThemeId("material_you") }
                            )
                        }
                    }
                }
            }
        }

        // ── Section: Language ─────────────────────────────────────────────────
        item {
            Text(
                context.getString(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        context.getString(R.string.settings_app_language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageButton(
                            label = context.getString(R.string.settings_language_english),
                            flag = "🇬🇧",
                            isSelected = appLanguage == "en",
                            onClick = {
                                if (appLanguage != "en") {
                                    pendingLanguageCode = "en"
                                    viewModel.setLanguage("en")
                                    showRestartDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageButton(
                            label = context.getString(R.string.settings_language_indonesian),
                            flag = "🇮🇩",
                            isSelected = appLanguage == "id",
                            onClick = {
                                if (appLanguage != "id") {
                                    pendingLanguageCode = "id"
                                    viewModel.setLanguage("id")
                                    showRestartDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Section: Downloads ───────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                context.getString(R.string.settings_content),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDownload() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            context.getString(R.string.settings_downloads),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            context.getString(R.string.settings_downloads_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // ── Section: Data ────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                context.getString(R.string.settings_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearBookmarksDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            context.getString(R.string.settings_clear_bookmarks),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            context.getString(R.string.settings_clear_bookmarks_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // ── Section: About ───────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                context.getString(R.string.settings_about),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        context.getString(R.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        context.getString(R.string.settings_version),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Bottom spacing
        item { Spacer(Modifier.height(16.dp)) }
    }

    // ── Clear Bookmarks Confirmation Dialog ──────────────────────────────────
    if (showClearBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showClearBookmarksDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(context.getString(R.string.bookmarks_clear_all) + "?") },
            text = {
                Text(context.getString(R.string.bookmarks_clear_confirm))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllBookmarks()
                    showClearBookmarksDialog = false
                }) {
                    Text(context.getString(R.string.bookmarks_clear_all), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBookmarksDialog = false }) {
                    Text(context.getString(R.string.common_cancel))
                }
            }
        )
    }
    
    // ── Restart App Dialog ───────────────────────────────────────────────────
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            icon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(context.getString(R.string.settings_language_changed)) },
            text = {
                Text(context.getString(R.string.settings_language_restart_message))
            },
            confirmButton = {
                TextButton(onClick = {
                    MainActivity.restart(context)
                }) {
                    Text(context.getString(R.string.settings_restart_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(context.getString(R.string.settings_restart_later))
                }
            }
        )
    }
}

// ── Theme Option Row ─────────────────────────────────────────────────────────

@Composable
private fun ThemeOptionRow(
    name: String,
    lightId: String,
    darkId: String,
    primaryLightColor: Color,
    bgLightColor: Color,
    primaryDarkColor: Color,
    bgDarkColor: Color,
    currentThemeId: String,
    onSelect: (String) -> Unit
) {
    val isLightSelected = currentThemeId == lightId
    val isDarkSelected = currentThemeId == darkId
    val isSelected = isLightSelected || isDarkSelected

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }

        // Light / Dark toggle row with color swatches
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Light variant
            ThemeVariantChip(
                label = "Light",
                primaryColor = primaryLightColor,
                bgColor = bgLightColor,
                isSelected = isLightSelected,
                onClick = { onSelect(lightId) },
                modifier = Modifier.weight(1f)
            )

            // Dark variant
            ThemeVariantChip(
                label = "Dark",
                primaryColor = primaryDarkColor,
                bgColor = bgDarkColor,
                isSelected = isDarkSelected,
                onClick = { onSelect(darkId) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeVariantChip(
    label: String,
    primaryColor: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary color swatch
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
            // Background color swatch
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            Text(
                if (label == "Light") context.getString(R.string.settings_theme_light)
                else context.getString(R.string.settings_theme_dark),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LanguageButton(
    label: String,
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                flag,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
