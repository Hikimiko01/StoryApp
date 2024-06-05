package com.dicoding.picodiploma.storyapp.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.dicoding.picodiploma.storyapp.R
import com.dicoding.picodiploma.storyapp.data.pref.UserModel
import com.dicoding.picodiploma.storyapp.databinding.ActivityLoginBinding
import com.dicoding.picodiploma.storyapp.view.ViewModelFactory
import com.dicoding.picodiploma.storyapp.view.main.MainActivity
import com.dicoding.picodiploma.storyapp.view.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
        setupTextWatchers()

    }

    private fun setupTextWatchers(){
        val emailEditText = binding.edLoginEmail
        val emailInputLayout = binding.emailEditTextLayout
        emailInputLayout.setEditText(emailEditText)

        val passwordEditText = binding.edLoginPassword
        val passwordInputLayout = binding.passwordEditTextLayout
        passwordInputLayout.setEditText(passwordEditText)

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
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            binding.loadingProgressBar.visibility = View.VISIBLE

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password) { response ->
                    // Menghilangkan progress bar ketika menerima respon
                    binding.loadingProgressBar.visibility = View.GONE

                    if (response.error == false) {
                        viewModel.saveSession(UserModel(response.loginResult?.name ?: "User" , email, response.loginResult?.token ?: ""))
                        AlertDialog.Builder(this).apply {
                            setTitle("Yeah!")
                            setMessage(getString(R.string.login_success))
                            setPositiveButton("Lanjut") { _, _ ->
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    } else {
                        AlertDialog.Builder(this).apply {
                            setTitle("Ups!")
                            setMessage(getString(R.string.acc_not_found))
                            setPositiveButton("Daftar") { _, _ ->
                                val intent = Intent(context, SignupActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }
                }
            } else {

                binding.loadingProgressBar.visibility = View.GONE
                AlertDialog.Builder(this).apply {
                    setTitle("Ups!")
                    setMessage(getString(R.string.field_not_filled))
                    setPositiveButton("Kembali") { _, _ -> }
                    create()
                    show()
                }
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                login
            )
            startDelay = 100
        }.start()
    }

}