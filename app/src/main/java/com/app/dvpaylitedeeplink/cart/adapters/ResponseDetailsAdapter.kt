package com.app.dvpaylitedeeplink.cart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.models.ResponseDetails

class ResponseDetailsAdapter(private val typesList: ArrayList<ResponseDetails>) :
    RecyclerView.Adapter<ResponseDetailsAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keyTextView: AppCompatTextView = itemView.findViewById(R.id.itt_keyTextView)
        val valueTextView: AppCompatTextView = itemView.findViewById(R.id.itt_valueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_response_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val details = typesList[position]

        holder.keyTextView.text = details.key
        holder.valueTextView.text = details.value
    }

    override fun getItemCount(): Int = typesList.size
}