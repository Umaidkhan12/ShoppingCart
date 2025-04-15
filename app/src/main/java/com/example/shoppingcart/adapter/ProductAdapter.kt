package com.example.shoppingcart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ItemProductBinding
import com.example.shoppingcart.model.Product

class ProductAdapter(
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) = with(binding) {
            // Product Name
            productName.text = product.name.ifBlank { context.getString(R.string.unknown_product) }

            // Price
            productPrice.text = try {
                context.getString(R.string.price_format, product.price)
            } catch (e: Exception) {
                context.getString(R.string.price_unavailable)
            }

            // Stock Status
            stockStatus.text = when {
                product.stock <= 0 -> context.getString(R.string.out_of_stock)
                product.stock < 5 -> context.getString(R.string.only_x_left, product.stock)
                else -> context.getString(R.string.in_stock)
            }

            // Add to Cart Button
            addToCartButton.apply {
                isEnabled = product.stock > 0
                setOnClickListener {
                    if (product.stock > 0) onAddToCartClick(product)
                }
            }

            // Product Image with fallback
            Glide.with(context)
                .load(product.imageUrl.takeIf { it.isNotBlank() })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.image_not_found)
                .into(productImage)
        }

        private val context get() = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductBinding.inflate(inflater, parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
