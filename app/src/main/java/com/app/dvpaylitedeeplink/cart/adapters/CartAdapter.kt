package com.app.dvpaylitedeeplink.cart.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.models.Item
import com.bumptech.glide.Glide

class CartAdapter(
    private val context: Context,
    private val items: MutableList<Item>,
    private val onItemClick: (Double, Int, Int) -> Unit, // Lambda for click events
    private val onCartUpdated: (Double) -> Unit // Lambda for cart total updates
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val cartItems = mutableMapOf<Int, Item>() // Stores items in the cart

    inner class CartViewHolder(val binding: View) : RecyclerView.ViewHolder(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        val itemImage = holder.itemView.findViewById<ImageView>(R.id.imageView)
        val itemName = holder.itemView.findViewById<TextView>(R.id.itemName)
        val itemPrice = holder.itemView.findViewById<TextView>(R.id.itemPrice)
        val itemQuantity = holder.itemView.findViewById<TextView>(R.id.itemQuantity)
        val incrementButton = holder.itemView.findViewById<Button>(R.id.incrementButton)
        val decrementButton = holder.itemView.findViewById<Button>(R.id.decrementButton)
        val cardView = holder.itemView.findViewById<CardView>(R.id.cardView)

        Glide.with(context)
            .load(item.image)
            .into(itemImage)
        itemName.text = item.name
        itemPrice.text = "$${String.format("%.2f", item.price)}"
        itemQuantity.text = cartItems[position]?.quantity?.toString() ?: "0"

        if (cartItems.containsKey(position)) {
            cardView.background =
                ContextCompat.getDrawable(context, R.drawable.rounded_outline_dark)
        } else {
            cardView.background =
                ContextCompat.getDrawable(context, R.drawable.rounded_outline_grey)
        }

        cardView.setOnClickListener {
            if (cartItems.containsKey(position)) {
                cartItems.remove(position)
            } else {
                cartItems[position] = item.copy(quantity = 1)
                onItemClick(item.price, 1, position) // Pass details to the activity
            }
            notifyItemChanged(position)
            updateCart()
        }

        itemName.setOnClickListener {
            if (cartItems.containsKey(position)) {
                cartItems.remove(position)
            } else {
                cartItems[position] = item.copy(quantity = 1)
                onItemClick(item.price, 1, position) // Pass details to the activity
            }
            notifyItemChanged(position)
            updateCart()
        }

        itemPrice.setOnClickListener {
            if (cartItems.containsKey(position)) {
                cartItems.remove(position)
            } else {
                cartItems[position] = item.copy(quantity = 1)
                onItemClick(item.price, 1, position) // Pass details to the activity
            }
            notifyItemChanged(position)
            updateCart()
        }

        incrementButton.setOnClickListener {
            cartItems[position]?.let {
                it.quantity++
                notifyItemChanged(position)
                updateCart()
            } ?: run {
                cartItems[position] = item.copy(quantity = 1)
                notifyItemChanged(position)
                updateCart()
            }
        }

        decrementButton.setOnClickListener {
            cartItems[position]?.let {
                if (it.quantity > 1) {
                    it.quantity--
                    notifyItemChanged(position)
                    updateCart()
                    onItemClick(it.price, it.quantity, position)
                } else {
                    cartItems.remove(position)
                    notifyItemChanged(position)
                    updateCart()
                }
            }
        }
    }

    fun itemAddToCart() {

    }

    fun getSelectedItems(): List<Item> {
        return cartItems.values.filter { it.quantity > 0 }
    }

    fun removeAllItems() {
        cartItems.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    private fun updateCart() {
        val totalAmount = cartItems.values.sumOf { it.price * it.quantity }
        onCartUpdated(totalAmount)
    }
}


