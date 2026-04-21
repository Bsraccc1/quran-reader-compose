package com.quranreader.custom.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranreader.custom.ui.viewmodel.PageViewModel

/**
 * Quran Page View - Displays a single Quran page
 * Loads from local storage first, downloads if needed
 * Automatically inverts colors in dark mode for better readability
 */
@Composable
fun QuranPageView(
    pageNumber: Int,
    modifier: Modifier = Modifier,
    viewModel: PageViewModel = hiltViewModel()
) {
    val pageState by viewModel.getPageState(pageNumber).collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    
    // Color matrix for inverting colors in dark mode
    val colorMatrix = remember(isDarkTheme) {
        if (isDarkTheme) {
            ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        } else {
            null
        }
    }
    
    LaunchedEffect(pageNumber) {
        viewModel.loadPage(pageNumber)
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = pageState) {
            is PageState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Loading page $pageNumber...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            is PageState.Success -> {
                Image(
                    bitmap = state.bitmap.asImageBitmap(),
                    contentDescription = "Quran Page $pageNumber",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) }
                )
            }
            
            is PageState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        "Failed to load page $pageNumber",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(
                        onClick = { viewModel.loadPage(pageNumber) }
                    ) {
                        Text("Retry")
                    }
                }
            }
            
            is PageState.Idle -> {
                // Initial state
            }
        }
    }
}

/**
 * Page loading states
 */
sealed class PageState {
    object Idle : PageState()
    object Loading : PageState()
    data class Success(val bitmap: Bitmap) : PageState()
    data class Error(val message: String) : PageState()
}
