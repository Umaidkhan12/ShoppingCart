package com.example.shoppingcart.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppingcart.adapter.CartAdapter
import com.example.shoppingcart.databinding.FragmentCartBinding
import com.example.shoppingcart.model.CartItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId get() = auth.currentUser?.uid.orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartAdapter = CartAdapter(cartItems).apply {
            onQuantityChanged = { item, newQuantity -> updateQuantity(item, newQuantity) }
            onItemRemoved = { item -> removeDeleteItem(item) }
        }

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter

        binding.checkoutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                Snackbar.make(requireView(), "Proceeding to checkout...", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(requireView(), "Cart is empty", Snackbar.LENGTH_SHORT).show()
            }
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        if (userId.isEmpty()) return

        db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                cartItems.clear()
                for (doc in result.documents) {
                    val item = doc.toObject(CartItem::class.java)
                    Log.d("CartFragment", "Item loaded: $item")
                    item?.let { cartItems.add(it) }
                }
                cartAdapter.notifyDataSetChanged()
                updateTotal()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to load cart", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (userId.isEmpty()) return
        val updatedItem = item.copy(quantity = newQuantity)
        db.collection("users")
            .document(userId)
            .collection("cart")
            .document(item.productId)
            .set(updatedItem)
            .addOnSuccessListener {
                cartAdapter.updateItem(updatedItem)
                updateTotal()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to update quantity", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun removeDeleteItem(item: CartItem) {
        if (userId.isEmpty()) return
        db.collection("users")
            .document(userId)
            .collection("cart")
            .document(item.productId)
            .delete()
            .addOnSuccessListener {
                cartAdapter.removeItem(item)
                updateTotal()
                Snackbar.make(requireView(), "Item removed from cart", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to remove item", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.totalText.text = "Total: ₹%.2f".format(total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
