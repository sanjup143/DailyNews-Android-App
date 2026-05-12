package com.sanju.newsapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sanju.newsapp.R
import com.sanju.newsapp.databinding.ItemNewsBinding
import com.sanju.newsapp.model.Article
import com.sanju.newsapp.utils.DateUtils

class NewsAdapter(
    private var bookmarkedUrls: Set<String>,
    private val onBookmarkClick: (Article) -> Unit,
    private val onItemClick: (Article) -> Unit
) : ListAdapter<Article, NewsAdapter.NewsViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    class NewsViewHolder(val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemId(position: Int): Long {
        return getItem(position).url.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = getItem(position)

        with(holder.binding) {

            tvTitle.text = article.title ?: root.context.getString(R.string.no_title)
            tvDescription.text = article.description ?: root.context.getString(R.string.no_description)
            tvSource.text = root.context.getString(R.string.news)
            tvDate.text = DateUtils.formatDate(article.publishedAt)

            Glide.with(imgNews.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fallback(R.drawable.placeholder)
                .centerCrop()
                .dontAnimate()
                .into(imgNews)

            val isBookmarked = bookmarkedUrls.contains(article.url)

            btnBookmark.setImageResource(
                if (isBookmarked) R.drawable.ic_bookmark_filled
                else R.drawable.ic_bookmark_border
            )

            btnBookmark.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBookmarkClick(getItem(pos))
                }
            }

            root.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }
    }

    // ✅ Efficient bookmark updates
    fun updateBookmarks(newBookmarks: Set<String>) {
        val oldBookmarks = bookmarkedUrls
        bookmarkedUrls = newBookmarks

        currentList.forEachIndexed { index, article ->
            val wasBookmarked = oldBookmarks.contains(article.url)
            val isBookmarked = newBookmarks.contains(article.url)

            if (wasBookmarked != isBookmarked) {
                notifyItemChanged(index)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}