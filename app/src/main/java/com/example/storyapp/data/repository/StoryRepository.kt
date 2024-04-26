package com.example.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.data.StoryPagingSource
import com.example.storyapp.data.api.ApiService
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.Story
import com.example.storyapp.data.response.StoryResponse

class StoryRepository(private val pref: UserPreference, private val apiService: ApiService) {

    suspend fun getStories(token: String, onSuccess: (List<ListStoryItem>) -> Unit, onError: (String) -> Unit) {
        try {
            val response = apiService.getStories("Bearer $token")
            if (response.error) {
                onError(response.message)
            } else {
                onSuccess(response.listStory)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    fun getStoryPager(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                StoryPagingSource(
                    apiService, pref, token)
            }
        ).liveData
    }

    suspend fun getStoryDetail(token: String, storyId: String): Story {
        val response = apiService.getStoryDetail("Bearer $token", storyId)
        if (response.error) {
            throw Exception(response.message)
        }
        return response.story
    }

    fun getStoryLocation(token: String): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoryLocation(token, 1)
            emit(Result.Success(response))
        } catch (e: java.lang.Exception) {
            Log.d("Signup", e.message.toString())
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getSession(): LiveData<UserModel> {
        return pref.getSession().asLiveData()
    }


    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        // Modify the getInstance function to accept both UserPreference and ApiService as parameters.
        fun getInstance(pref: UserPreference, apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(pref, apiService).also { instance = it }
            }
    }
}