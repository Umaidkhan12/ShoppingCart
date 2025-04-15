package com.example.shoppingcart.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        setupLogoutButton()
        loadAdditionalUserData()
    }

    private fun setupUserProfile() {
        if (currentUser != null) {
            // User is logged in - show actual profile
            binding.displayName.text = currentUser.displayName ?: getString(R.string.default_display_name)
            binding.email.text = currentUser.email ?: getString(R.string.email_not_available)

            currentUser.photoUrl?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.image_not_found)
                    .circleCrop()
                    .into(binding.profileImage)
            } ?: run {
                binding.profileImage.setImageResource(R.drawable.placeholder_profile)
            }

            // Show logout button
            binding.logoutButton.apply {
                text = getString(R.string.logout)
                setOnClickListener { showLogoutConfirmation() }
            }
        } else {
            // User is not logged in - show guest UI
            binding.displayName.text = getString(R.string.guest_user)
            binding.email.text = getString(R.string.login_prompt)
            binding.profileImage.setImageResource(R.drawable.placeholder_profile)

            // Convert logout button to login button
            binding.logoutButton.apply {
                text = getString(R.string.login_signup)
                setOnClickListener {
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
            }
        }
    }

    private fun loadAdditionalUserData() {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        // Example: Load phone number from Firestore
                        val phoneNumber = it.getString("phoneNumber")
                        // Update UI with additional data
                    }
                }
                .addOnFailureListener {
                    showErrorSnackbar("Failed to load user data")
                }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout_confirmation_title))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                FirebaseAuth.getInstance().signOut()
                setupUserProfile() // Update UI immediately after logout
                findNavController().navigate(R.id.action_profileFragment_to_productFragment)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performLogout() {
        auth.signOut()
        findNavController().navigate(R.id.action_profileFragment_to_productFragment)
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.errorColor))
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupUserProfile() // Refresh UI when returning from login flow
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}