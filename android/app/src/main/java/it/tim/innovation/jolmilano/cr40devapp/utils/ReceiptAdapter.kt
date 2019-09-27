/* MIT License

Copyright (c) 2019 TIM S.p.A.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */
package it.tim.innovation.jolmilano.cr40devapp.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.model.ReceiptLine

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class ReceiptAdapter internal constructor(val context: Context) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems = emptyList<ReceiptLine>()//Cached copy of Lines
    private lateinit var mOnItemActionListener: OnItemActionListener

    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener{

        val name = itemView.findViewById(R.id.productName) as TextView
        val brand = itemView.findViewById(R.id.productBrand) as TextView
        val total = itemView.findViewById(R.id.productTotal) as TextView
        val quantity = itemView.findViewById(R.id.quantity) as TextView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            mOnItemActionListener.onItemLongClick(mItems[adapterPosition])
            return true
        }

        override fun onClick(v: View?) {
            mOnItemActionListener.onItemClick(mItems[adapterPosition])
        }
    }

    fun getItems(): List<ReceiptLine> = mItems

    fun setOnItemActionListener(listener: OnItemActionListener){
        mOnItemActionListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val itemView = inflater.inflate(R.layout.receipt_item, parent, false)
        return ReceiptViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {

        val item = mItems[position]

        holder.quantity.text = item.quantity.toString()
        holder.name.text = item.prodName
        holder.brand.text = String.format(context.getString(R.string.brand_string, item.prodBrand))
        holder.total.text = (item.price * item.quantity).div(100.00).toString()
    }

    override fun getItemCount(): Int = mItems.size

    internal fun setItems(items: List<ReceiptLine>){
        this.mItems = items
        notifyDataSetChanged()
    }

    interface OnItemActionListener{
        fun onItemClick(item: ReceiptLine)
        fun onItemLongClick(item: ReceiptLine)
    }
}