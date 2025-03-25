package com.app.dvpaylitedeeplink.cart.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.interfaces.TypeSelectionInterface

class TransTypesAdapter(private val context: Context, private val typesList: ArrayList<String>, private val typeSelectionInterface: TypeSelectionInterface) :
    RecyclerView.Adapter<TransTypesAdapter.ViewHolder>() {

    /*private var selectedPosition: Int = RecyclerView.NO_POSITION*/
    private var selectedPosition: Int = 0
    /*private var listItemClickListener: ListItemClickListener? = null

    fun setListItemClickListener(listener: ListItemClickListener) {
        this.listItemClickListener = listener
    }*/

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeCardView: CardView = itemView.findViewById(R.id.itt_transactionTypeCardView)
        val typeTextView: AppCompatTextView =
            itemView.findViewById(R.id.itt_transactionTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trans_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val types = typesList[position]

        holder.typeTextView.text = types.toString()
        if (position == selectedPosition) {
            holder.typeCardView.setCardBackgroundColor(context.resources.getColor(R.color.pur_500))
            holder.typeTextView.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder.typeCardView.setCardBackgroundColor(context.resources.getColor(R.color.white))
            holder.typeTextView.setTextColor(context.resources.getColor(R.color.pur_500))
        }

        holder.itemView.setOnClickListener {
            selectItem(holder)
        }
    }

    private fun selectItem(holder: ViewHolder) {
        val adapterPosition = holder.adapterPosition
        val oldPosition = selectedPosition
        selectedPosition = adapterPosition

        notifyItemChanged(oldPosition) // Deselect previous item
        notifyItemChanged(selectedPosition) // Select new item
        typeSelectionInterface.onSelected(typesList[adapterPosition])
    }

    override fun getItemCount(): Int = typesList.size
}