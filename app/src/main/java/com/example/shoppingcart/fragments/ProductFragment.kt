package com.example.shoppingcart.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppingcart.R
import com.example.shoppingcart.adapter.ProductAdapter
import com.example.shoppingcart.databinding.FragmentProductBinding
import com.example.shoppingcart.model.Product
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadProductsFromFirebase()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                addToCart(user.uid, product)
            } else {
                Snackbar.make(binding.root, "Please log in to add to cart", Snackbar.LENGTH_LONG)
                    .setAction("Login") {
                        // Navigate to login screen if needed
                        findNavController().navigate(R.id.loginFragment)
                    }
                    .show()
            }
        }


        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProductFragment.adapter
        }
    }

    private fun addToCart(userId: String, product: Product) {
        val cartItem = hashMapOf(
            "productId" to product.id,
            "name" to product.name,
            "price" to product.price,
            "imageUrl" to product.imageUrl,
            "quantity" to 1
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("cart")
            .document(product.id)
            .set(cartItem)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "${product.name} added to cart", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Failed to add to cart", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun loadProductsFromFirebase() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }
                adapter.submitList(products)
            }
            .addOnFailureListener { exception ->
                Log.e("ProductFragment", "Error fetching products", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}