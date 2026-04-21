package com.quranreader.custom.ui.screens.reading

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranreader.custom.R
import com.quranreader.custom.data.QuranInfo
import com.quranreader.custom.ui.components.CircularReadingProgress
import com.quranreader.custom.ui.viewmodel.ReadingViewModel
import com.quranreader.custom.ui.viewmodel.SessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    initialPage: Int,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMushaf: (Int) -> Unit = {},
    viewModel: ReadingViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionState by viewModel.sessionState.collectAsState()
    val newSessionLimit by viewModel.newSessionLimit.collectAsState()
    val continueReadingLimit by viewModel.continueReadingLimit.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    
    var sessionTargetInput by remember { mutableStateOf("") }
    
    val totalPages = 604
    val progress = currentPage.toFloat() / totalPages.toFloat()
    
    val currentSurah = remember(currentPage) {
        QuranInfo.getSurahEnglishName(
            (1..114).firstOrNull { 
                QuranInfo.getStartPage(it) <= currentPage 
            } ?: 1
        )
    }
    val currentAyah = 1

    // Navigate to mushaf only once when session starts
    val sessionStartPage by viewModel.sessionStartPage.collectAsState()
    
    // FIX F-003: Use only sessionStartPage as LaunchedEffect key
    // Using hasNavigatedToMushaf as key causes re-trigger when flag changes
    LaunchedEffect(sessionStartPage) {
        val startPage = sessionStartPage
        val hasNavigated = viewModel.hasNavigatedToMushaf.value
        
        if (startPage != null && !hasNavigated) {
            viewModel.markNavigatedToMushaf()
            onNavigateToMushaf(startPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.reading_daily_quran),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onNavigateToSearch) {
                Icon(Icons.Outlined.Search, contentDescription = context.getString(R.string.nav_search))
            }
        }

        Spacer(Modifier.weight(0.3f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularReadingProgress(
                progress = progress,
                currentPage = currentPage,
                totalPages = totalPages,
                currentSurah = currentSurah,
                currentAyah = currentAyah
            )
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(
            visible = sessionState == SessionState.IDLE || 
                     sessionState == SessionState.ACTIVE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (sessionState) {
                    SessionState.IDLE -> {
                        Button(
                            onClick = {
                                sessionTargetInput = newSessionLimit.toString()
                                viewModel.showStartSessionInput()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                context.getString(R.string.reading_start_session), 
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    SessionState.ACTIVE -> {
                        OutlinedButton(
                            onClick = {
                                sessionTargetInput = continueReadingLimit.toString()
                                viewModel.showContinueSessionInput()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Outlined.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                context.getString(R.string.reading_continue_session), 
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        AnimatedVisibility(
            visible = sessionState == SessionState.INPUT_PENDING,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = context.getString(R.string.reading_session_target),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sessionTargetInput,
                        onValueChange = { 
                            sessionTargetInput = it.filter { c -> c.isDigit() }
                        },
                        label = { Text(context.getString(R.string.reading_pages_count)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val pages = sessionTargetInput.toIntOrNull() 
                                    ?: newSessionLimit
                                if (viewModel.sessionStartPage.value == null) {
                                    viewModel.startNewSession(pages)
                                } else {
                                    viewModel.continueSessionWithPages(pages)
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.width(160.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.cancelSessionInput() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(context.getString(R.string.reading_cancel))
                        }

                        Button(
                            onClick = {
                                val pages = sessionTargetInput.toIntOrNull() 
                                    ?: newSessionLimit
                                if (viewModel.sessionStartPage.value == null) {
                                    viewModel.startNewSession(pages)
                                } else {
                                    viewModel.continueSessionWithPages(pages)
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(context.getString(R.string.reading_start))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(0.7f))

        if (sessionState == SessionState.IDLE) {
            Text(
                text = context.getString(R.string.reading_click_to_start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}
