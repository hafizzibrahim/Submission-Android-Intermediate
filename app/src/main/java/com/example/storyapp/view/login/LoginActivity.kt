package com.example.storyapp.view.login

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.data.api.ApiConfig
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.view.MainActivity
import com.example.storyapp.view.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        setupView()
        setupAction()
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

    private fun setupAction() {
        val progressBar = binding.progressBar
        binding.loginButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val client = ApiConfig.getApiService().login(email, password)
            client.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    progressBar.visibility = View.GONE
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (!responseBody.error) {
                            viewModel.saveSession(
                                UserModel(
                                    responseBody.loginResult.userId,
                                    responseBody.loginResult.name,
                                    responseBody.loginResult.token,
                                    true
                                )
                            )
                            AlertDialog.Builder(this@LoginActivity).apply {
                                setTitle("Yeah!")
                                setMessage("Anda berhasil login. Sudah tidak sabar untuk belajar ya?")
                                setPositiveButton("Lanjut") { _, _ ->
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            AlertDialog.Builder(this@LoginActivity).apply {
                                setTitle("Ooops!")
                                setMessage("Login failed")
                                setPositiveButton("Lanjut") { _, _ ->
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        }
                    } else {
                        AlertDialog.Builder(this@LoginActivity).apply {
                            setTitle("Ooops!")
                            setMessage("Login failed")
                            setPositiveButton("Lanjut") { _, _ ->
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: ${t.message}")
                    AlertDialog.Builder(this@LoginActivity).apply {
                        setTitle("Oops!")
                        setMessage("${t.message}")
                        setPositiveButton("OK") { _, _ -> }
                        create()
                        show()
                    }
                }
            })
        }
    }
}

