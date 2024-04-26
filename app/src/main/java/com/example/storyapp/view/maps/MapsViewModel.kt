package com.example.storyapp.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.response.ListStoryItem

class MapsViewModel(private val repository: StoryRepository): ViewModel() {

    fun getStoryLocation(token: String) =
        repository.getStoryLocation(token)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession()
    }
}