package com.android.stocksapp

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_stock.view.*
import java.util.*

class StockRecyclerAdapter(var list: ArrayList<Stock>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as MyViewHolder
        with(holder.itemView) {
            ticker.text = list[position].name.toUpperCase()
            price.text = String.format("%.2f", list[position].price)
            lastUpdate.text = getDate(list[position].lastUpdate)
            if (list[position].increase == 1)
                price.setBackgroundColor(Color.GREEN)
            else if (list[position].increase == -1)
                price.setBackgroundColor(Color.RED)
        }

    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun getDate(millis: Long): String {
        val date = Date()
        date.time = millis
        return DateUtils.getRelativeTimeSpanString(
            date.time,
            Calendar.getInstance().timeInMillis,
            DateUtils.SECOND_IN_MILLIS
        ).toString()
    }

    public fun updateList(arrayList: ArrayList<Stock>) {
        list.clear()
        list.addAll(arrayList)
        notifyDataSetChanged()
    }

    public fun addItem(stock: Stock) {
        list.add(stock)
        notifyItemInserted(list.size - 1)
    }

    public fun changeItem(stock: Stock, position: Int) {
        list[position] = stock
        notifyItemChanged(position)
    }

}