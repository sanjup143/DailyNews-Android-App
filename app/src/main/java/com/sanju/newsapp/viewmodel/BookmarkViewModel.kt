package com.sanju.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val bookmarks = repository.getBookmarks().asLiveData()

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val isBookmarked = repository.isBookmarked(article.url)
            if (isBookmarked) {
                repository.removeBookmark(article)
            } else {
                repository.addBookmark(article)
            }
        }
    }
}