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
package it.tim.innovation.jolmilano.cr40devapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.utils.ItemsAdapter
import it.tim.innovation.jolmilano.cr40devapp.utils.Utils
import it.tim.innovation.jolmilano.cr40devapp.viewmodels.ProductsViewModel
import it.tim.innovation.jolmilano.cr40devapp.viewmodels.TransactionsViewModel
import kotlinx.android.synthetic.main.fragment_list_products.*

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class ProductsListFragment : Fragment() {

    private lateinit var mProductsViewModel: ProductsViewModel
    private lateinit var mTransactionViewModel: TransactionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { fragmentActivity ->
            mProductsViewModel = ViewModelProviders.of(this).get(ProductsViewModel::class.java)
            mTransactionViewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
            val listAdapter = ItemsAdapter(fragmentActivity)
            listAdapter.setOnItemActionListener(object : ItemsAdapter.OnItemActionListener {
                override fun onItemLongClick(item: Item) {
                    val builder = AlertDialog.Builder(fragmentActivity)
                    builder.setTitle(R.string.delete_product_dialog_title)
                    builder.setMessage(R.string.delete_product_dialog_message)
                    builder.setPositiveButton(R.string.yes) { dialog, which ->
                        mProductsViewModel.deleteItem(item)
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(R.string.no) { dialog, which ->
                        dialog.dismiss()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }

                override fun onItemClick(item: Item) {
                    mTransactionViewModel.insertItem(item)
                }
            })
            productsList.adapter = listAdapter
            productsList.layoutManager = LinearLayoutManager(fragmentActivity)

            mProductsViewModel.getAllItems().observe(this, Observer { list ->
                if (list.isNotEmpty()) {
                    listAdapter.setItems(list)
                    productsList.visibility = View.VISIBLE
                    noItemsTv.visibility = View.GONE
                } else {
                    productsList.visibility = View.GONE
                    noItemsTv.visibility = View.VISIBLE
                }
            })

            deleteAll.setOnClickListener {
                val builder = AlertDialog.Builder(fragmentActivity)
                builder.setTitle(R.string.delete_all_products_dialog_title)
                builder.setMessage(R.string.delete_all_products_dialog_message)
                builder.setPositiveButton(R.string.yes) { dialog, which ->
                    mProductsViewModel.deleteAllItems()
                    Utils.hideSoftKeyboard(fragmentActivity)
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.no) { dialog, which ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
}