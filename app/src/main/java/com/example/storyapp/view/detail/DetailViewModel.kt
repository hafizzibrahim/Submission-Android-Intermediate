package com.example.storyapp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.response.Story
import kotlinx.coroutines.launch

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _story = MutableLiveData<Story>()
    val story: LiveData<Story> get() = _story

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchStoryDetail(token: String, storyId: String) {
        viewModelScope.launch {
            try {
                val storyDetail = storyRepository.getStoryDetail(token, storyId)
                _story.value = storyDetail
            } catch (e: Exception) {
                _error.value = "An error occurred while fetching story details."
            }
        }
    }
}
