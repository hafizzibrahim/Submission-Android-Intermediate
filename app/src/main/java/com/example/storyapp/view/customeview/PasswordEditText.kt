package com.example.storyapp.view.customeview

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.example.storyapp.R

@SuppressLint("ClickableViewAccessibility")
class PasswordEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    private var isPasswordVisible = false

    init {
        updatePasswordVisibility()
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (right - drawableEnd.bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // ...
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val (isValid, errorMessage) = isPasswordValid()
                if (!isValid) {
                    error = errorMessage
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // ...
            }
        })
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        updatePasswordVisibility()
    }

    private fun updatePasswordVisibility() {
        transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            0, 0, if (isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24, 0
        )
    }

    fun isPasswordValid(): Pair<Boolean, String> {
        val password = text.toString()
        return if (password.length >= 8) {
            Pair(true, "Password is valid")
        } else {
            Pair(false, "Password must be at least 8 characters long")
        }
    }
}


