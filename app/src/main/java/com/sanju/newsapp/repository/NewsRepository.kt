package com.sanju.newsapp.repository

import com.sanju.newsapp.model.Article
import com.sanju.newsapp.model.ArticleDao
import com.sanju.newsapp.network.NewsApi
import com.sanju.newsapp.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val api: NewsApi,
    private val dao: ArticleDao
) {

    // ================= API =================

    suspend fun searchNews(query: String, page: Int): List<Article> {
        return api.searchNews(
            query = query,
            page = page,
            sortBy = Constants.SORT_BY,
            language = Constants.LANGUAGE
        ).articles
    }

    suspend fun getNewsByCategory(category: String?, page: Int): List<Article> {
        return api.getTopHeadlines(
            country = Constants.COUNTRY,
            category = category,
            page = page
        ).articles
    }

    // ================= ROOM =================

    suspend fun addBookmark(article: Article) {
        dao.insert(article)
    }

    suspend fun removeBookmark(article: Article) {
        dao.delete(article)
    }

    fun getBookmarks(): Flow<List<Article>> {
        return dao.getAllArticles()
    }

    suspend fun isBookmarked(url: String): Boolean {
        return dao.isBookmarkedSync(url)
    }
}