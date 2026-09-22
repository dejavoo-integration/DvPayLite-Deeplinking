package com.app.dvpaylitedeeplink.dialogs

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.adapters.ResponseDetailsAdapter
import com.app.dvpaylitedeeplink.cart.models.LoadItems
import com.app.dvpaylitedeeplink.cart.models.ResponseDetails
import org.json.JSONObject

class TxnCompletePopUp(private val activity: Activity) {

    var transactionResult:JSONObject? = null

    /*fun setTransactionResult(transactionResult:JSONObject?){
        this.transactionResult = transactionResult
    }*/



    fun showPopUpDialog(transactionStatus:Boolean,type:String,resultMessage:String) {
        val layoutInflater = LayoutInflater.from(activity as Context)
        val dialogView: View = layoutInflater.inflate(R.layout.layout_popup, null)

        val builder = AlertDialog.Builder(activity as Context)
            .setView(dialogView)
            .setCancelable(false) // Prevent dialog from being dismissed by outside touch or back button

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val statusImageView = dialogView.findViewById<AppCompatImageView>(R.id.lp_statusImageView)
        val statusMessageTextView = dialogView.findViewById<AppCompatTextView>(R.id.lp_statusMessageTextView)
        val statusDescriptionTextView = dialogView.findViewById<AppCompatTextView>(R.id.lp_statusDescriptionTextView)
        val closeButton = dialogView.findViewById<AppCompatButton>(R.id.lp_doneButton)
        val transactionDetailsTextView = dialogView.findViewById<AppCompatTextView>(R.id.lp_transactionDetailsTextView)
        val detailsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.lp_detailsRecyclerView)

        if (transactionStatus){
            statusImageView.setImageResource(R.drawable.success)
            closeButton.setBackgroundDrawable(activity.resources.getDrawable(R.drawable.rounded_btn_green))
            when(type){
                LoadItems.TRANSACTION->{
                    statusMessageTextView.text = "Transaction Success"
                    statusDescriptionTextView.text = "Transaction Completed Successfully"
                }
                LoadItems.SETTLEMENT->{
                    statusMessageTextView.text = "Batch Settlement Success"
                    statusDescriptionTextView.text = "Batch Settled Successfully"
                }
            }
        }else{
            statusImageView.setImageResource(R.drawable.failure)
            when(type){
                LoadItems.TRANSACTION->{
                    statusMessageTextView.text = "Transaction Failed"
                    statusDescriptionTextView.text = resultMessage
                }
                LoadItems.SETTLEMENT->{
                    statusMessageTextView.text = "Batch Settlement Failed"
                    statusDescriptionTextView.text = resultMessage
                }
            }
            closeButton.setBackgroundDrawable(activity.resources.getDrawable(R.drawable.rounded_btn_red))
        }

        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }



        if (transactionResult!=null){
            transactionDetailsTextView.visibility = View.VISIBLE
            val transactionResultList = extractJsonObjectToArrayList(transactionResult!!)
            transactionDetailsTextView.setOnClickListener{
                loadDetailsIntoRecyclerView(detailsRecyclerView,transactionResultList)
            }
        }

        alertDialog.show()
    }

    private fun extractJsonObjectToArrayList(jsonObject:JSONObject):ArrayList<ResponseDetails>{
        val responseDetailsList = ArrayList<ResponseDetails>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key).toString()
            responseDetailsList.add(ResponseDetails(key,value))
        }
        return responseDetailsList
    }

    private fun loadDetailsIntoRecyclerView(detailsRecyclerView:RecyclerView,responseDetailsList:ArrayList<ResponseDetails>){
        detailsRecyclerView.visibility = View.VISIBLE
        val staggeredGridLayoutManager = GridLayoutManager(activity as Context, 2)
        detailsRecyclerView.layoutManager = staggeredGridLayoutManager
        detailsRecyclerView.adapter = ResponseDetailsAdapter(responseDetailsList)
    }


}