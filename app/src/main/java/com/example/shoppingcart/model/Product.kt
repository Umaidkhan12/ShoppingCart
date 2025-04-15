package com.example.shoppingcart.model

data class Product(
    var id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val stock: Int = 0
)