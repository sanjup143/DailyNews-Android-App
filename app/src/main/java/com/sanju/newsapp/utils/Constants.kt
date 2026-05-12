package com.sanju.newsapp.utils

object Constants {

    // 🌐 API CONFIG
    const val BASE_URL = "https://newsapi.org/v2/"
    const val COUNTRY = "us"

    // 📄 PAGINATION
    const val PAGE_SIZE = 20
    const val STARTING_PAGE_INDEX = 1

    // 🔍 SEARCH
    const val MIN_SEARCH_QUERY_LENGTH = 3
    const val SEARCH_DEBOUNCE_DELAY = 500L

    // 📂 CATEGORIES
    const val CATEGORY_GENERAL = "general"
    const val CATEGORY_BUSINESS = "business"
    const val CATEGORY_TECHNOLOGY = "technology"
    const val CATEGORY_SPORTS = "sports"
    const val CATEGORY_HEALTH = "health"

    // FILTER

    const val SORT_BY = "publishedAt"
    const val LANGUAGE = "en"

    // ⏱ NETWORK
    const val NETWORK_TIMEOUT = 30L

    // ERROR
    const val ERROR_NO_INTERNET =
        "No internet connection. Please check your network and try again."
}