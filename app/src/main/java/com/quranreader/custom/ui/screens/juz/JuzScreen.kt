package com.quranreader.custom.ui.screens.juz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranreader.custom.R
import com.quranreader.custom.data.QuranNavigationData
import com.quranreader.custom.data.model.HizbInfo
import com.quranreader.custom.data.model.JuzInfo
import com.quranreader.custom.data.model.QuranNavigationTab
import com.quranreader.custom.data.model.SurahInfo

/**
 * Juz Screen with Tab Selection — NO TopAppBar
 * Displays Juz, Surah, or Hizb based on selected tab
 */
@Composable
fun JuzScreen(
    onNavigateToReading: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(QuranNavigationTab.JUZ) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab Row
        TabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Content based on selected tab
        when (selectedTab) {
            QuranNavigationTab.JUZ -> JuzList(onNavigateToReading)
            QuranNavigationTab.SURAH -> SurahList(onNavigateToReading)
            QuranNavigationTab.HIZB -> HizbList(onNavigateToReading)
        }
    }
}

@Composable
private fun TabSelector(
    selectedTab: QuranNavigationTab,
    onTabSelected: (QuranNavigationTab) -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(
            text = context.getString(R.string.juz_tab),
            isSelected = selectedTab == QuranNavigationTab.JUZ,
            onClick = { onTabSelected(QuranNavigationTab.JUZ) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = context.getString(R.string.surah_tab),
            isSelected = selectedTab == QuranNavigationTab.SURAH,
            onClick = { onTabSelected(QuranNavigationTab.SURAH) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = context.getString(R.string.hizb_tab),
            isSelected = selectedTab == QuranNavigationTab.HIZB,
            onClick = { onTabSelected(QuranNavigationTab.HIZB) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// JUZ LIST
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun JuzList(onNavigateToReading: (Int) -> Unit) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(QuranNavigationData.juzList) { juz ->
            JuzCard(
                juz = juz,
                onClick = { onNavigateToReading(juz.startPage) }
            )
        }
    }
}

@Composable
private fun JuzCard(
    juz: JuzInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Juz Number Badge
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        juz.number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Juz Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    context.getString(R.string.juz_number, juz.number),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    context.getString(R.string.juz_pages, juz.startPage, juz.endPage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    "${juz.endPage - juz.startPage + 1} ${context.getString(R.string.juz_pages, 0, 0).split(" ")[0].lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            // Arrow Icon
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SURAH LIST
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SurahList(onNavigateToReading: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(QuranNavigationData.surahList) { surah ->
            SurahCard(
                surah = surah,
                onClick = { onNavigateToReading(surah.startPage) }
            )
        }
    }
}

@Composable
private fun SurahCard(
    surah: SurahInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Badge
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        surah.number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Surah Info
            Column(modifier = Modifier.weight(1f)) {
                // Arabic Name
                Text(
                    surah.arabicName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(2.dp))
                
                // English Name
                Text(
                    surah.englishName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Ayah count and type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        context.getString(R.string.surah_ayahs, surah.ayahCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Text("•", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (surah.isMakki) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            if (surah.isMakki) 
                                context.getString(R.string.surah_makki) 
                            else 
                                context.getString(R.string.surah_madani),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (surah.isMakki) 
                                MaterialTheme.colorScheme.onTertiaryContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Arrow Icon
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HIZB LIST
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HizbList(onNavigateToReading: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(QuranNavigationData.hizbList) { hizb ->
            HizbCard(
                hizb = hizb,
                onClick = { onNavigateToReading(hizb.startPage) }
            )
        }
    }
}

@Composable
private fun HizbCard(
    hizb: HizbInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hizb Badge with Quarter indicator
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        hizb.number.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "¼ ${hizb.quarter}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Hizb Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    context.getString(R.string.hizb_number, hizb.number),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(2.dp))
                
                Text(
                    context.getString(R.string.hizb_quarter, hizb.quarter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        context.getString(R.string.juz_number, hizb.juzNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Text("•", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    
                    Text(
                        context.getString(R.string.home_page) + " ${hizb.startPage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Arrow Icon
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
