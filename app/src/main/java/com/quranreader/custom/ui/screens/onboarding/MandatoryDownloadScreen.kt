package com.quranreader.custom.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranreader.custom.ui.viewmodel.DownloadViewModel

/**
 * Mandatory Download Screen - Shown on first app launch
 * User can download Quran pages or skip to use online mode
 */
@Composable
fun MandatoryDownloadScreen(
    onDownloadComplete: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel()
) {
    val downloadStats by viewModel.downloadStats.collectAsState()
    val downloadingPages by viewModel.downloadingPages.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadMessage by viewModel.downloadMessage.collectAsState()
    val downloadedMB by viewModel.downloadedMB.collectAsState()
    val totalMB by viewModel.totalMB.collectAsState()
    
    // Check if download is complete
    LaunchedEffect(downloadStats) {
        if (downloadStats.pagesDownloaded >= downloadStats.totalPages) {
            onDownloadComplete()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Welcome Text
            Text(
                "Welcome to Daily Quran",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Download Quran pages for offline reading, or skip to use online mode",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Download Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Download Quran Pages",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "604 pages • ~30MB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    if (downloadingPages) {
                        // Show progress
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Text(
                                "$downloadProgress% • ${downloadStats.pagesDownloaded} / ${downloadStats.totalPages} pages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(Modifier.height(4.dp))
                            
                            Text(
                                "%.1f MB / %.1f MB".format(downloadedMB, totalMB),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            downloadMessage?.let { message ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Show download button
                        Button(
                            onClick = { viewModel.downloadAllPages() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Start Download",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Skip Button
            if (!downloadingPages) {
                TextButton(
                    onClick = onDownloadComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Skip - Use Online Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(Modifier.height(16.dp))
            }
            
            // Info Text
            Text(
                if (downloadingPages) 
                    "Please keep the app open until download completes"
                else
                    "Download once for offline reading, or skip to stream pages online",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (downloadingPages) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = if (downloadingPages) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
