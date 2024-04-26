package com.example.storyapp.view.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.api.ApiConfig
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.view.login.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    showPasswordError()
                } else {
                    binding.passwordEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.buttonRegister.setOnClickListener {
            val progressBar = binding.progressBar
            val name = binding.namaEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (isEmailValid(email)) {
                progressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    try {
                        val response = ApiConfig.getApiService().register(name, email, password)
                        progressBar.visibility = View.GONE

                        if (!response.error) {
                            showSuccessDialog()
                        } else {
                            showErrorDialog(response.message)
                        }
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        Log.e("Error", e.toString())
                    }
                }
            } else {
                binding.emailEditText.error = "Invalid email address"
            }
        }
    }

    private fun showPasswordError() {
        binding.passwordEditText.error = "Password must be at least 8 characters long"
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage("Anda berhasil daftar. Sudah tidak sabar untuk belajar ya?")
            setPositiveButton("Lanjut") { _, _ ->
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Ooops!")
            setMessage(errorMessage)
            setPositiveButton("Oke") { _, _ ->
                val intent = Intent(this@RegisterActivity, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }
}
