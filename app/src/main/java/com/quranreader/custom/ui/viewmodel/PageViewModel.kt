package com.quranreader.custom.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranreader.custom.data.page.QuranPageProvider
import com.quranreader.custom.ui.components.PageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Quran page loading
 */
@HiltViewModel
class PageViewModel @Inject constructor(
    private val pageProvider: QuranPageProvider
) : ViewModel() {
    
    // Cache for page states
    private val pageStates = mutableMapOf<Int, MutableStateFlow<PageState>>()
    
    /**
     * Get state flow for a specific page
     */
    fun getPageState(pageNumber: Int): StateFlow<PageState> {
        return pageStates.getOrPut(pageNumber) {
            MutableStateFlow(PageState.Idle)
        }.asStateFlow()
    }
    
    /**
     * Load a page - checks local first, downloads if needed
     */
    fun loadPage(pageNumber: Int) {
        val stateFlow = pageStates.getOrPut(pageNumber) {
            MutableStateFlow(PageState.Idle)
        }
        
        // Don't reload if already loaded
        if (stateFlow.value is PageState.Success) {
            return
        }
        
        viewModelScope.launch {
            stateFlow.value = PageState.Loading
            
            val result = pageProvider.getPageImage(pageNumber)
            
            stateFlow.value = if (result.isSuccess) {
                result.getOrNull()?.let { bitmap ->
                    PageState.Success(bitmap)
                } ?: PageState.Error("Failed to decode image")
            } else {
                PageState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Preload adjacent pages for smooth scrolling
     */
    fun preloadAdjacentPages(currentPage: Int) {
        viewModelScope.launch {
            // Preload previous page
            if (currentPage > 1) {
                loadPage(currentPage - 1)
            }
            
            // Preload next page
            if (currentPage < 604) {
                loadPage(currentPage + 1)
            }
        }
    }
    
    /**
     * Clear cache for memory management
     */
    fun clearCache() {
        pageStates.clear()
    }
}
