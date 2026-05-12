package com.sanju.newsapp.viewmodel

import androidx.lifecycle.*
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.repository.NewsRepository
import com.sanju.newsapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Article>>(emptyList())
    val searchResults: LiveData<List<Article>> = _searchResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentQuery = ""
    private var currentPage = Constants.STARTING_PAGE_INDEX
    private var isLastPage = false
    private var isRequestRunning = false

    private var searchJob: Job? = null

    // ===============================
    // SEARCH
    // ===============================
    fun onSearchQueryChanged(query: String) {

        if (query == currentQuery) return

        currentQuery = query
        resetState()

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(Constants.SEARCH_DEBOUNCE_DELAY)

            // Prevent outdated execution after cancel
            if (!isActive) return@launch

            searchNews()
        }
    }

    // ===============================
    // PAGINATION
    // ===============================
    fun loadNextPage() {
        if (isRequestRunning || isLastPage) return
        searchNews()
    }

    // ===============================
    // CORE SEARCH
    // ===============================
    private fun searchNews() {

        if (currentQuery.isBlank() || isRequestRunning) return

        viewModelScope.launch {

            isRequestRunning = true
            _isLoading.value = true

            try {

                val newData = repository.searchNews(
                    query = currentQuery,
                    page = currentPage
                )

                if (newData.isEmpty()) {
                    isLastPage = true
                } else {

                    val updatedList = if (currentPage == Constants.STARTING_PAGE_INDEX) {
                        newData
                    } else {
                        _searchResults.value.orEmpty() + newData
                    }

                    _searchResults.value = updatedList

                    // Pagination check
                    if (newData.size < Constants.PAGE_SIZE) {
                        isLastPage = true
                    } else {
                        currentPage++
                    }
                }

                _error.value = null

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = Constants.ERROR_NO_INTERNET
            } finally {
                _isLoading.value = false
                isRequestRunning = false
            }
        }
    }

    // ===============================
    // RESET
    // ===============================
    private fun resetState() {
        currentPage = Constants.STARTING_PAGE_INDEX
        isLastPage = false
        isRequestRunning = false
        _searchResults.value = emptyList()
        _error.value = null
    }
}