package com.sanju.newsapp.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.ui.adapter.NewsAdapter
import com.sanju.newsapp.R
import com.sanju.newsapp.databinding.FragmentCategoryNewsBinding
import com.sanju.newsapp.viewmodel.BookmarkViewModel
import com.sanju.newsapp.viewmodel.CategoryNewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryNewsFragment : Fragment(R.layout.fragment_category_news) {

    private var _binding: FragmentCategoryNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryNewsViewModel by viewModels()
    private val bookmarkViewModel: BookmarkViewModel by viewModels()

    private lateinit var adapter: NewsAdapter
    private val bookmarkedUrls = mutableSetOf<String>()

    private var category: String? = null

    companion object {
        fun newInstance(category: String?): CategoryNewsFragment {
            return CategoryNewsFragment().apply {
                arguments = Bundle().apply {
                    putString("category", category)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCategoryNewsBinding.bind(view)

        category = arguments?.getString("category")

        setupRecyclerView()
        setupSwipeRefresh()
        observeData()

        viewModel.setCategory(category)
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            bookmarkedUrls = bookmarkedUrls,

            onBookmarkClick = { article ->
                bookmarkViewModel.toggleBookmark(article)
            },

            onItemClick = { article ->
                val bundle = bundleOf("article" to article)
                findNavController().navigate(R.id.articleDetailFragment, bundle)
            }
        )

        val layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            this.adapter = this@CategoryNewsFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var isLoadingMore = false

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    val shouldPaginate =
                        !isLoadingMore &&
                                (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 3)

                    if (shouldPaginate) {
                        isLoadingMore = true
                        viewModel.loadNextPage()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        isLoadingMore = false
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeData() {

        viewModel.articles.observe(viewLifecycleOwner) { articles: List<Article> ->
            if (!isAdded) return@observe

            binding.errorLayout.isVisible = false
            adapter.submitList(articles.toList())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading: Boolean ->
            binding.swipeRefresh.isRefreshing = false

            val shouldShowShimmer =
                isLoading && adapter.itemCount == 0

            binding.shimmerLayout.isVisible = shouldShowShimmer
            binding.recyclerView.isVisible = !shouldShowShimmer

            if (shouldShowShimmer) {
                binding.shimmerLayout.startShimmer()
            } else {
                binding.shimmerLayout.stopShimmer()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error: String? ->
            binding.errorLayout.isVisible = error != null && adapter.itemCount == 0
            if (error != null) {
                binding.tvErrorTitle.text = getString(R.string.oops)
                binding.tvErrorMessage.text = error
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }

        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner) { articles: List<Article> ->
            val newBookmarks = articles.map { article -> article.url }.toSet()
            adapter.updateBookmarks(newBookmarks)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}