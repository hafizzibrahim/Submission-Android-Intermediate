package com.example.storyapp.view.AddStories

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresExtension
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.view.MainActivity
import com.example.storyapp.R
import com.example.storyapp.data.api.ApiConfig
import com.example.storyapp.data.response.FileUploadResponse
import com.example.storyapp.databinding.ActivityAddStoriesBinding
import com.example.storyapp.view.getImageUri
import com.example.storyapp.view.reduceFileImage
import com.example.storyapp.view.uriToFile
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class AddStoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoriesBinding
    private var currentImageUri: Uri? = null
    private var token : String? = null
//    private lateinit var apiService: ApiService

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("token").toString()
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { token?.let { it1 -> uploadImage("Bearer $it1") } }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun uploadImage(token: String) {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.editTextText.text.toString()

            if (description.isNotEmpty() && imageFile.exists()) {
                showLoading(true)

                val requestBody = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo",
                    imageFile.name,
                    requestImageFile
                )
                lifecycleScope.launch {
                    try {
                        val apiService = ApiConfig.getApiService().addStory(token, requestBody, multipartBody)
                        apiService.enqueue(object : Callback<FileUploadResponse> {
                            override fun onResponse(
                                call: Call<FileUploadResponse>,
                                response: Response<FileUploadResponse>
                            ) {
                                showToast(response.message())
                                showLoading(false)
                                val intent = Intent(this@AddStoriesActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }

                            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                                showToast("Gagal")
                                showLoading(false)
                            }
                        })
                    } catch (e: HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                        val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                        showToast(errorResponse.message)
                        showLoading(false)
                    }
                }
            } else {
                showToast("Gagal")
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}