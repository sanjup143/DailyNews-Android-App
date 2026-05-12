package com.sanju.newsapp.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.sanju.newsapp.R
import com.sanju.newsapp.databinding.FragmentArticleDetailBinding
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.viewmodel.BookmarkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleDetailFragment : Fragment(R.layout.fragment_article_detail) {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val bookmarkViewModel: BookmarkViewModel by viewModels()

    private var article: Article? = null
    private var isBookmarked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArticleDetailBinding.bind(view)

        setupToolbar()
        getArticleFromArgs()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {

                R.id.action_bookmark -> {
                    article?.let { bookmarkViewModel.toggleBookmark(it) }
                    true
                }

                R.id.action_share -> {
                    article?.let { shareArticle(it) }
                    true
                }

                else -> false
            }
        }
    }

    private fun getArticleFromArgs() {
        val args = ArticleDetailFragmentArgs.fromBundle(requireArguments())
        article = args.article

        article?.let {
            setupUI(it)
            observeBookmarkState(it)
            handleReadFullArticle(it)
        } ?: run {
            Toast.makeText(requireContext(), getString(R.string.error_article_not_found), Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupUI(article: Article) {
        Glide.with(requireContext())
            .load(article.urlToImage)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(binding.imgDetail)

        binding.tvDetailTitle.text = article.title ?: getString(R.string.no_title)
        binding.tvDetailContent.text = article.description ?: article.content ?: ""
        binding.tvDetailDate.text = article.publishedAt ?: ""
    }

    private fun observeBookmarkState(article: Article) {
        // ✅ Explicit type added → fixes error
        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks: List<Article> ->
            isBookmarked = bookmarks.any { bookmarkedArticle ->
                bookmarkedArticle.url == article.url
            }
            updateBookmarkIcon()
        }
    }

    private fun updateBookmarkIcon() {
        val icon = if (isBookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_border
        }
        binding.toolbar.menu.findItem(R.id.action_bookmark)?.setIcon(icon)
    }

    private fun handleReadFullArticle(article: Article) {
        binding.btnReadMore.setOnClickListener {
            if (isInternetAvailable()) {
                val bundle = Bundle().apply {
                    putString("url", article.url)
                }
                findNavController().navigate(R.id.webviewFragment, bundle)
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareArticle(article: Article) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, article.url)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)))
    }

    private fun isInternetAvailable(): Boolean {
        val cm = requireContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}