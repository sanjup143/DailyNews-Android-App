package com.sanju.newsapp.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.sanju.newsapp.ui.adapter.NewsAdapter
import com.sanju.newsapp.R
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.ui.adapter.NewsPagerAdapter
import com.sanju.newsapp.utils.Constants
import com.sanju.newsapp.viewmodel.BookmarkViewModel
import com.sanju.newsapp.viewmodel.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleListFragment : Fragment(R.layout.fragment_article_list) {

    private val viewModel: NewsViewModel by viewModels()
    private val bookmarkViewModel: BookmarkViewModel by viewModels()

    private lateinit var searchAdapter: NewsAdapter
    private val bookmarkedUrls = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val searchView = view.findViewById<TextInputEditText>(R.id.searchView)
        val rvSearchResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val errorLayout = view.findViewById<View>(R.id.errorLayout)
        val btnRetry = view.findViewById<View>(R.id.btnRetry)

        val tvErrorTitle = view.findViewById<android.widget.TextView>(R.id.tvErrorTitle)
        val tvErrorMessage = view.findViewById<android.widget.TextView>(R.id.tvErrorMessage)

        val searchShimmerLayout = view.findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.searchShimmerLayout)

        // ---------------- VIEWPAGER SETUP ----------------
        val adapter = NewsPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
        val tabTitles = listOf(getString(R.string.top),
            getString(R.string.business),
            getString(R.string.technology),
            getString(R.string.sports),
            getString(R.string.health)
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // ---------------- SEARCH ADAPTER ----------------
        searchAdapter = NewsAdapter(
            bookmarkedUrls = bookmarkedUrls,
            onBookmarkClick = { article ->
                bookmarkViewModel.toggleBookmark(article)
            },
            onItemClick = { article ->
                val action =
                    ArticleListFragmentDirections
                        .actionArticleListFragmentToArticleDetailFragment(article)

                findNavController().navigate(action)
            }
        )

        val layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.layoutManager = layoutManager
        rvSearchResults.adapter = searchAdapter

        // ---------------- PAGINATION ----------------
        rvSearchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        // ---------------- SEARCH ----------------
        searchView.addTextChangedListener { editable ->

            val query = editable?.toString().orEmpty()
            val isValidQuery = query.length >= Constants.MIN_SEARCH_QUERY_LENGTH

            viewPager.isVisible = query.isEmpty()
            tabLayout.isVisible = query.isEmpty()
            rvSearchResults.isVisible = isValidQuery

            if (isValidQuery) {
                viewModel.onSearchQueryChanged(query)
            } else {
                searchAdapter.submitList(emptyList())
                errorLayout.isVisible = false
            }
        }

        // ---------------- OBSERVE SEARCH ----------------
        viewModel.searchResults.observe(viewLifecycleOwner) { articles ->
            searchAdapter.submitList(articles)
            errorLayout.isVisible =
                articles.isEmpty() && searchView.text?.isNotEmpty() == true
        }

        // ---------------- LOADING ----------------
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading: Boolean ->
            val shouldShowShimmer =
                isLoading && searchAdapter.itemCount == 0

            searchShimmerLayout.isVisible = shouldShowShimmer
            rvSearchResults.isVisible = !shouldShowShimmer && searchView.text.toString().length >= Constants.MIN_SEARCH_QUERY_LENGTH

            if (shouldShowShimmer) {
                searchShimmerLayout.startShimmer()
            } else {
                searchShimmerLayout.stopShimmer()
            }
        }

        // ---------------- ERROR ----------------
        viewModel.error.observe(viewLifecycleOwner) { error: String? ->
            if (error != null && searchAdapter.itemCount == 0) {
                errorLayout.isVisible = true
                viewPager.isVisible = false
                tvErrorTitle.text = getString(R.string.oops)
                tvErrorMessage.text = error
            }
        }

        // ---------------- RETRY ----------------
        btnRetry.setOnClickListener {
            errorLayout.isVisible = false
            viewPager.isVisible = true

            val query = searchView.text?.toString().orEmpty()
            if (query.length >= Constants.MIN_SEARCH_QUERY_LENGTH) {
                viewModel.onSearchQueryChanged(query)
            }
        }

        // ---------------- BOOKMARK SYNC ----------------
        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner) { articles: List<Article> ->
            val newBookmarks = articles.map { article -> article.url }.toSet()
            searchAdapter.updateBookmarks(newBookmarks)
        }
    }
}