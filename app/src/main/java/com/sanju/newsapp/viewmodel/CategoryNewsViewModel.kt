package com.sanju.newsapp.viewmodel

import androidx.lifecycle.*
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.repository.NewsRepository
import com.sanju.newsapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryNewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>(emptyList())
    val articles: LiveData<List<Article>> = _articles

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage = Constants.STARTING_PAGE_INDEX
    private var currentCategory: String? = null

    private var isLastPage = false
    private var isRequestRunning = false

    // ===============================
    // SET CATEGORY
    // ===============================
    fun setCategory(category: String?) {

        // Avoid unnecessary reload
        if (category == currentCategory && !_articles.value.isNullOrEmpty()) return

        currentCategory = category
        resetState()
        loadNews()
    }

    // ===============================
    // PAGINATION
    // ===============================
    fun loadNextPage() {
        if (isRequestRunning || isLastPage) return
        loadNews()
    }

    // ===============================
    // REFRESH
    // ===============================
    fun refresh() {
        resetState()
        loadNews()
    }

    // ===============================
    // CORE API CALL
    // ===============================
    private fun loadNews() {

        if (isRequestRunning) return

        viewModelScope.launch {

            isRequestRunning = true
            _isLoading.value = true

            try {
                val newData = repository.getNewsByCategory(
                    category = currentCategory,
                    page = currentPage
                )

                if (newData.isEmpty()) {
                    isLastPage = true
                } else {

                    val updatedList = if (currentPage == Constants.STARTING_PAGE_INDEX) {
                        newData
                    } else {
                        _articles.value.orEmpty() + newData
                    }

                    _articles.value = updatedList

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
            }finally {
                _isLoading.value = false
                isRequestRunning = false
            }
        }
    }

    // ===============================
    // RESET STATE
    // ===============================
    private fun resetState() {
        currentPage = Constants.STARTING_PAGE_INDEX
        isLastPage = false
        isRequestRunning = false
        _articles.value = emptyList()
        _error.value = null
    }
}