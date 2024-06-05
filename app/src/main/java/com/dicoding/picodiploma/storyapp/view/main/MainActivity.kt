package com.dicoding.picodiploma.storyapp.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.storyapp.R
import com.dicoding.picodiploma.storyapp.databinding.ActivityMainBinding
import com.dicoding.picodiploma.storyapp.view.ViewModelFactory
import com.dicoding.picodiploma.storyapp.view.add.AddStoryActivity
import com.dicoding.picodiploma.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                binding.greetingTv.text = binding.root.context.getString(R.string.greeting, user.name ?: "User")
            }
        }

        setupView()
        setupRecyclerView()
        setupViewModel()
        setupAction()
        addStory()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getSession().observe(this) { user ->
            loadStories(user.token)
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter()
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = itemAdapter
    }

    private fun setupViewModel() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                loadStories(user.token)
            }
        }

        viewModel.stories.observe(this) { stories ->
            Log.d("MainActivity", "Updating adapter with stories: $stories")
            itemAdapter.submitList(stories)
            binding.loadingProgressBar.visibility = View.GONE
        }
    }

    private fun setupAction() {
        binding.actionLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun loadStories(token: String) {
        binding.loadingProgressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getStories(token)
        }
    }

    private fun addStory(){
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }
}