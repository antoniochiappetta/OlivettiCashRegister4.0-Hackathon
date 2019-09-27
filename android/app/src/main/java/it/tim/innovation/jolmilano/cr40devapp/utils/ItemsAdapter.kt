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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.model.Item

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class ItemsAdapter internal constructor(context: Context) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems = emptyList<Item>()//Cached copy of Items
    private lateinit var mOnItemActionListener: OnItemActionListener

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener{

        val image = itemView.findViewById(R.id.productImage) as ImageView
        val product = itemView.findViewById(R.id.productName) as TextView
        val brand = itemView.findViewById(R.id.productBrand) as TextView
        val price = itemView.findViewById(R.id.productPrice) as TextView

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

    fun setOnItemActionListener(listener: OnItemActionListener){
        mOnItemActionListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = inflater.inflate(R.layout.product_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = mItems[position]

        holder.product.text = item.product
        holder.brand.text = item.brand
        holder.price.text = item.price.div(Constants.RECEIPT_ROUND).toString()
    }

    override fun getItemCount(): Int = mItems.size

    internal fun setItems(items: List<Item>){
        this.mItems = items
        notifyDataSetChanged()
    }

    interface OnItemActionListener{
        fun onItemClick(item: Item)
        fun onItemLongClick(item: Item)
    }
}