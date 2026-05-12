package com.sanju.newsapp

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sanju.newsapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")

        val navController = navHostFragment.navController

        // Bottom navigation setup
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener { }

        // Back press handling
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {

                if (navController.navigateUp()) return

                if (binding.bottomNavigationView.selectedItemId != R.id.articleListFragment) {
                    binding.bottomNavigationView.selectedItemId = R.id.articleListFragment
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // Destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBackPressState(destination.id)
            updateBottomNavVisibility(destination.id)
        }
    }

    private fun updateBackPressState(destinationId: Int) {
        backPressedCallback.isEnabled =
            destinationId != R.id.articleListFragment &&
                    destinationId != R.id.bookmarksFragment
    }

    private fun updateBottomNavVisibility(destinationId: Int) {
        val shouldShow =
            destinationId == R.id.articleListFragment ||
                    destinationId == R.id.bookmarksFragment

        val targetTranslation = if (shouldShow) 0f else binding.bottomNavigationView.height.toFloat()

        // Avoid unnecessary animation calls
        if (binding.bottomNavigationView.translationY != targetTranslation) {
            binding.bottomNavigationView.animate()
                .translationY(targetTranslation)
                .setDuration(200)
                .start()
        }

        binding.bottomNavigationView.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
    }
}