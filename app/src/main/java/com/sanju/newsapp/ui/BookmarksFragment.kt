package com.sanju.newsapp.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanju.newsapp.R
import com.sanju.newsapp.ui.adapter.NewsAdapter
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.databinding.FragmentBookmarksBinding
import com.sanju.newsapp.viewmodel.BookmarkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val bookmarkViewModel: BookmarkViewModel by viewModels()

    private lateinit var newsAdapter: NewsAdapter
    private val bookmarkedUrls = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBookmarksBinding.bind(view)

        setupRecyclerView()
        observeBookmarks()
    }

    private fun setupRecyclerView() {

        newsAdapter = NewsAdapter(
            bookmarkedUrls = bookmarkedUrls,

            onItemClick = { article ->
                val action =
                    BookmarksFragmentDirections
                        .actionBookmarksFragmentToArticleDetailFragment(article)

                findNavController().navigate(action)
            },

            onBookmarkClick = { article ->
                bookmarkViewModel.toggleBookmark(article)
            }
        )

        binding.recyclerViewBookmarks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun observeBookmarks() {
        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner) { articles: List<Article> ->
            val newBookmarks = articles.map { article -> article.url }.toSet()

            newsAdapter.submitList(articles)
            newsAdapter.updateBookmarks(newBookmarks)

            binding.tvEmpty.isVisible = articles.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}