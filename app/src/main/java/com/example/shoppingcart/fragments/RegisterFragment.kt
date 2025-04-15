package com.example.shoppingcart.fragments

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (validateInputs(email, password, confirmPassword)) {
                registerUser(email, password)
            }
        }

        binding.loginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Valid email required"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = "Minimum 6 characters required"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        if (confirmPassword != password) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun registerUser(email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (!isAdded) return@addOnCompleteListener // Check fragment is attached
                showLoading(false)

                if (task.isSuccessful) {
                    sendVerificationEmail()
                    navigateToLogin()
                } else {
                    showError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (isAdded && view != null) { // Check fragment is still attached
                    if (task.isSuccessful) {
                        Snackbar.make(
                            requireView(), // Use requireView() instead of binding.root
                            "Verification email sent",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.action_registerFragment_to_loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.registerFragment, true)
                .build()
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}