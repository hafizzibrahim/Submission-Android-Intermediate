package com.example.storyapp.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryPagingSource
import com.example.storyapp.data.adapter.StoryAdapter
import com.example.storyapp.data.adapter.StoryClickListener
import com.example.storyapp.data.api.ApiConfig
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.pref.dataStore
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.view.AddStories.AddStoriesActivity
import com.example.storyapp.view.detail.DetailActivity
import com.example.storyapp.view.maps.MapsActivity
import com.example.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), StoryClickListener {
    private lateinit var binding: ActivityMainBinding
    private val apiService = ApiConfig.getApiService()
    private lateinit var pref: UserPreference
    private val storyRepository: StoryRepository by lazy { StoryRepository(pref, apiService) }
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var storyAdapter: PagingDataAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>
    private var token: String? = null
    private lateinit var pagingDataFlow: Flow<PagingData<ListStoryItem>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi UserPreference
        pref = UserPreference.getInstance(this@MainActivity.dataStore)

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                token = user.token
                setupView()
                fetchStories()
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        setupAction()
    }

    private fun setupView() {
        storyAdapter = StoryAdapter()
        (storyAdapter as StoryAdapter).setStoryClickListener(this)
        binding.rvStories.adapter = storyAdapter

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }


    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
        addStory()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun fetchStories() {
        val adapter = storyAdapter
        binding.rvStories.adapter = adapter
        token?.let {
            viewModel.quote(it).observe(this) {
                adapter.submitData(lifecycle, it)
            }
        }
    }


    private fun addStory() {
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddStoriesActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        binding.fabMaps.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStoryClick(story: ListStoryItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("storyId", story.id.toString())
        intent.putExtra("token", token)
        startActivity(intent)
    }
}


