package com.sanju.newsapp.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sanju.newsapp.ui.CategoryNewsFragment
import com.sanju.newsapp.utils.Constants

class NewsPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    private val categories = listOf(
        Constants.CATEGORY_GENERAL,
        Constants.CATEGORY_BUSINESS,
        Constants.CATEGORY_TECHNOLOGY,
        Constants.CATEGORY_SPORTS,
        Constants.CATEGORY_HEALTH
    )

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]

        val apiCategory = if (category == Constants.CATEGORY_GENERAL) null else category

        return CategoryNewsFragment.newInstance(apiCategory)
    }

    override fun getItemId(position: Int): Long {
        return categories[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return categories.any { it.hashCode().toLong() == itemId }
    }
}