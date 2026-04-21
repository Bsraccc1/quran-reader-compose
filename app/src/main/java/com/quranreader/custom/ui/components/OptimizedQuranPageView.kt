package com.quranreader.custom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.quranreader.custom.data.QuranInfo

/**
 * Optimized Quran Page View using Coil.
 * Displays the mushaf image with surah/juz/page overlays at top corners and bottom center.
 */
@Composable
fun OptimizedQuranPageView(
    pageNumber: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Color matrix for dark mode inversion
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
        } else null
    }

    val pageStr = String.format("%03d", pageNumber)
    val imageUrl = "https://android.quran.com/data/width_800/page$pageStr.png"

    val imageRequest = remember(pageNumber) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(200)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .scale(Scale.FIT)
            .build()
    }

    // Compute surah and juz for this page
    val surahName = remember(pageNumber) { QuranInfo.getSurahNameForPage(pageNumber) }
    val juzNumber = remember(pageNumber) { QuranInfo.getJuzForPage(pageNumber) }

    // Overlay colors — always readable regardless of theme
    val overlayBg = if (isDarkTheme)
        Color(0xCC1A1A1A) else Color(0xCCFFFFFF)
    val overlayText = if (isDarkTheme)
        Color(0xFFE0E0E0) else Color(0xFF1A1A1A)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = "Quran Page $pageNumber",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Loading صفحة $pageNumber…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Failed to load page $pageNumber\nCheck your connection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )

        // Overlays removed to prevent covering Quran text
        // Surah and Juz info can be shown in the app bar instead

        // ── Bottom-center: Page number ────────────────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(6.dp),
            color = overlayBg,
            tonalElevation = 0.dp
        ) {
            Text(
                text = "صفحة $pageNumber",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = overlayText,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Preload adjacent pages for smooth scrolling
 */
@Composable
fun PreloadAdjacentPages(currentPage: Int) {
    val context = LocalContext.current
    LaunchedEffect(currentPage) {
        listOf(currentPage - 1, currentPage + 1)
            .filter { it in 1..604 }
            .forEach { page ->
                val pageStr = String.format("%03d", page)
                ImageRequest.Builder(context)
                    .data("https://android.quran.com/data/width_800/page$pageStr.png")
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
    }
}
