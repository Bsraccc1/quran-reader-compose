package com.quranreader.custom.ui.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.quranreader.custom.data.QuranInfo
import com.quranreader.custom.data.model.AyahCoordinate
import com.quranreader.custom.data.model.HighlightedAyah
import java.io.File

/**
 * Interactive Quran Page View with ayah-level tap detection and highlighting
 * Displays the mushaf image with surah/juz/page overlays and ayah highlights
 */
@Composable
fun InteractiveQuranPageView(
    pageNumber: Int,
    ayahCoordinates: List<AyahCoordinate>,
    highlightedAyah: HighlightedAyah?,
    onAyahTapped: (Int, Int, Int) -> Unit, // page, surah, ayah
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

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
    
    // Improved local file checking with validation
    val localFile = remember(pageNumber) {
        val candidates = listOf(
            File(context.filesDir, "pages/width_800/page$pageStr.png"),
            File(context.filesDir, "pages/page_$pageStr.png"),
            File(context.filesDir, "pages/page$pageStr.png"),
            File(context.filesDir, "pages/width_800/page_$pageStr.png")
        )
        candidates.firstOrNull { file ->
            file.exists() && file.length() > 1024 && // At least 1KB
            try {
                // Quick validation by checking if it's a valid image
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, options)
                options.outWidth > 0 && options.outHeight > 0
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Multiple fallback URLs for better reliability
    val fallbackUrls = remember(pageNumber) {
        listOf(
            "https://android.quran.com/data/width_800/page$pageStr.png",
            "https://cdn.qurancdn.com/images/w800/page_$pageStr.png",
            "https://quran-pages.herokuapp.com/api/pages/$pageNumber"
        )
    }
    
    val imageSource: Any = localFile ?: fallbackUrls.first()

    val imageRequest = remember(pageNumber, imageSource, localFile) {
        ImageRequest.Builder(context)
            .data(imageSource)
            .crossfade(200)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .scale(Scale.FIT)
            .error(android.R.drawable.ic_dialog_alert) // Show error icon on failure
            .listener(
                onError = { _, result ->
                    Log.e("QuranPageView", "Failed to load page $pageNumber: ${result.throwable.message}")
                }
            )
            .build()
    }

    // Compute surah and juz for this page
    val surahName = remember(pageNumber) { QuranInfo.getSurahNameForPage(pageNumber) }
    val juzNumber = remember(pageNumber) { QuranInfo.getJuzForPage(pageNumber) }

    // Overlay colors
    val overlayBg = if (isDarkTheme)
        Color(0xCC1A1A1A) else Color(0xCCFFFFFF)
    val overlayText = if (isDarkTheme)
        Color(0xFFE0E0E0) else Color(0xFF1A1A1A)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
    ) {
        // Quran page image
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = "Quran Page $pageNumber",
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(ayahCoordinates, viewSize) {
                    detectTapGestures { offset ->
                        if (viewSize.width > 0 && viewSize.height > 0) {
                            // Convert tap position to normalized coordinates
                            val normalizedX = offset.x / viewSize.width
                            val normalizedY = offset.y / viewSize.height

                            // Find tapped ayah
                            val tappedAyah = ayahCoordinates.firstOrNull { coord ->
                                coord.contains(normalizedX, normalizedY)
                            }

                            tappedAyah?.let {
                                onAyahTapped(it.page, it.surah, it.ayah)
                            }
                        }
                    }
                },
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
                        .padding(16.dp),
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

        // Ayah highlight overlay
        if (highlightedAyah != null && highlightedAyah.page == pageNumber && viewSize.width > 0) {
            val highlightCoord = ayahCoordinates.firstOrNull {
                it.surah == highlightedAyah.surah && it.ayah == highlightedAyah.ayah
            }

            highlightCoord?.let { coord ->
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rect = Rect(
                        left = coord.minX * size.width,
                        top = coord.minY * size.height,
                        right = coord.maxX * size.width,
                        bottom = coord.maxY * size.height
                    )

                    // Draw highlight
                    drawRect(
                        color = if (highlightedAyah.isBookmarked) {
                            Color(0x4000FF00) // Green for bookmarked
                        } else {
                            Color(0x40FFD700) // Gold for selected
                        },
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.width, rect.height)
                    )

                    // Draw border
                    drawRect(
                        color = if (highlightedAyah.isBookmarked) {
                            Color(0xFF00FF00)
                        } else {
                            Color(0xFFFFD700)
                        },
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.width, rect.height),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
            }
        }

        // Overlays removed to prevent covering Quran text
        // Surah and Juz info can be shown in the app bar instead

        // Bottom-center: Page number
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
