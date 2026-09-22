package com.app.dvpaylitedeeplink.cart.models

data class Cart(
    var amounts: MutableList<Amount> = mutableListOf(),
    var items: MutableList<Item> = mutableListOf()
)

data class Amount(
    var name: String,
    var value: Double
)

data class Item(
    var image : Int,
    var name: String,
    var price: Double,
    var quantity: Int,
    var additionalInfo: String,
    var customInfo: List<CustomInfo>? = null, // Optional field
    var modifiers: List<Modifier>? = null // Optional field
)

data class Modifier(
    var name: String,
    var options: List<Option>? = null // Optional field
)
data class CustomInfo(
    var name: String,
    var value: Double,
)

data class Option(
    var name: String,
    var price: Double,
    var quantity: Int
)

