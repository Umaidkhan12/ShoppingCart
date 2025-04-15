package com.example.shoppingcart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ItemCartBinding
import com.example.shoppingcart.model.CartItem

class CartAdapter(private val items: MutableList<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    var onQuantityChanged: ((CartItem, Int) -> Unit)? = null
    var onItemRemoved: ((CartItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            productName.text = item.name
            productPrice.text = "₹ ${item.price}"
            quantityText.text = item.quantity.toString()

            Glide.with(root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(productImage)

            increaseButton.setOnClickListener {
                val newQuantity = item.quantity + 1
                onQuantityChanged?.invoke(item, newQuantity)
            }

            decreaseButton.setOnClickListener {
                val newQuantity = item.quantity - 1
                if (newQuantity > 0) {
                    onQuantityChanged?.invoke(item, newQuantity)
                }
            }

            removeButton.setOnClickListener {
                onItemRemoved?.invoke(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItem(updatedItem: CartItem) {
        val index = items.indexOfFirst { it.productId == updatedItem.productId }
        if (index != -1) {
            items[index] = updatedItem
            notifyItemChanged(index)
        }
    }

    fun removeItem(itemToRemove: CartItem) {
        val index = items.indexOfFirst { it.productId == itemToRemove.productId }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}