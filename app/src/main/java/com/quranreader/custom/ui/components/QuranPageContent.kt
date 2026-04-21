package com.quranreader.custom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class for Ayah text
 */
data class Ayah(
    val surah: Int,
    val ayah: Int,
    val text: String,
    val page: Int
)

/**
 * Uthmanic font family for Quran text
 * Note: Font file must be in res/font/ folder with lowercase name
 */
private val UthmanicFontFamily = try {
    FontFamily(
        Font(com.quranreader.custom.R.font.uthmanic_hafs_ver12)
    )
} catch (e: Exception) {
    FontFamily.Default
}

/**
 * Text-based Quran page rendering
 * Uses Uthmanic font for proper Arabic display
 * Adapts to light/dark themes automatically
 */
@Composable
fun QuranPageContent(
    pageNumber: Int,
    ayahs: List<Ayah>,
    modifier: Modifier = Modifier
) {
    if (ayahs.isEmpty()) {
        // Loading state
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading page $pageNumber...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Page header
            item {
                Text(
                    text = "صفحة $pageNumber",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Ayahs
            items(ayahs) { ayah ->
                Text(
                    text = ayah.text,
                    fontFamily = UthmanicFontFamily,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 42.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Rtl
                    )
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
