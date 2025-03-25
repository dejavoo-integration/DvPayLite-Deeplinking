package com.app.dvpaylitedeeplink.cart.models

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dvpaylitedeeplink.R
import com.app.dvpaylitedeeplink.cart.adapters.TransTypesAdapter
import com.app.dvpaylitedeeplink.cart.interfaces.TypeSelectionInterface

class LoadItems {

    val cart = Cart(
        amounts = mutableListOf(
            Amount("Discounts", 00.00),
            Amount("Subtotal", 00.00),
            Amount("Total", 00.00)
        ),
        items = mutableListOf(
            Item(
                R.drawable.ic_icecream_pista, "Gelato Ice-Cream", 110.00, 0, "", listOf(CustomInfo("tax", 0.00), CustomInfo("Discount", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 110.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_cup_choco2, "Italian Ice", 70.00, 0, "", listOf(CustomInfo("tax", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 70.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_biscuit, "Ice Cream Biscuit", 45.00, 0, "", listOf(CustomInfo("tax", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 50.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_orange, "Orange Ice Cream", 90.00, 0, "", listOf(CustomInfo("tax", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 50.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_ic, "Ice Special", 50.00, 0, "", listOf(CustomInfo("tax", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 50.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_strawberry, "Frozen Yogurt", 270.30, 0, "",listOf(CustomInfo("tax", 0.00)),listOf(
                    Modifier("Toppings", listOf(Option("Pista", 270.30, 1)))
                )
            ),
            Item(
                R.drawable.ic_pizza2, "Pizza", 250.20, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Pizza", listOf(Option("Chicj", 250.20, 1)))
                )
            ),
            Item(
                R.drawable.ic_burger, "Burger", 150.00, 0,"", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Modifier", listOf(Option("Value 2", 150.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_strwberry_faluda, "Japanese Mochi Ice-Cream ", 25.56, 0, "", listOf(
                    CustomInfo("tax",0.00)
                ), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),
            Item(
                R.drawable.ic_food_cream, "cream", 25.56, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),
            Item(
                R.drawable.ic_cream_pair, "Cream Pair", 25.56, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),
            Item(
                R.drawable.ic_faluda, "Ice Falooda", 25.56, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),
            Item(
                R.drawable.ic_oreo, "Oreo Ice cream", 25.56, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),

            Item(
                R.drawable.ic_icream4, "Ice cream", 25.56, 0, "", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Vanilla", 25.56, 1)))
                )
            ),
            Item(
                R.drawable.ic_icream3, "Strawberry Cream", 10.00, 0, "", listOf(CustomInfo("tax", 9.00)),listOf(
                    Modifier("Toppings", listOf(Option("Caramel Drizzle", 10.00, 1)))
                )
            ),
            Item(
                R.drawable.ic_icreambb, "Ice cream", 35.10, 0,"", listOf(CustomInfo("tax",0.00)), listOf(
                    Modifier("Flavors", listOf(Option("Blueberry", 233.00, 1)))
                )
            )
        )
    )

    companion object {
        const val TRANSACTION: String = "TRANSACTION"
        const val SALE: String = "SALE"
        const val REFUND: String = "REFUND"
        const val PRE_AUTH: String = "PRE_AUTH"
        const val VOID: String = "VOID"
        const val TICKET: String = "TICKET"
        const val SETTLEMENT: String = "SETTLEMENT"
    }

    fun loadTransactionTypes(context:Context,transactionTypesRecyclerView:RecyclerView,typeSelectionInterface: TypeSelectionInterface){
        val transactionTypesList = ArrayList<String>()
        transactionTypesList.add(SALE)
        transactionTypesList.add(REFUND)
        transactionTypesList.add(PRE_AUTH)
        transactionTypesList.add(VOID)
        transactionTypesList.add(TICKET)
        transactionTypesList.add(SETTLEMENT)

        transactionTypesRecyclerView.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.HORIZONTAL,false)
        val transDetailAdapter = TransTypesAdapter(context,transactionTypesList,typeSelectionInterface)
        transactionTypesRecyclerView.adapter = transDetailAdapter
    }


}