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

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import it.tim.innovation.jolmilano.cr40devapp.MainActivity
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.utils.ConfigurationManager
import it.tim.innovation.jolmilano.cr40devapp.utils.Constants
import it.tim.innovation.jolmilano.cr40devapp.utils.InsertFragmentInterface
import it.tim.innovation.jolmilano.cr40devapp.utils.Utils
import it.tim.innovation.jolmilano.cr40devapp.viewmodels.ProductsViewModel
import it.tim.innovation.jolmilano.cr40devapp.viewmodels.TransactionsViewModel
import kotlinx.android.synthetic.main.fragment_insert_product.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Gaetano Dati on 04/07/2019
 */
class InsertProductFragment : Fragment(), InsertFragmentInterface {

    private val TAG = InsertProductFragment::class.java.name

    private var mInputList = arrayListOf<Int>()

    private lateinit var mProductsViewModel: ProductsViewModel
    private lateinit var mTransactionsViewModel: TransactionsViewModel
    private var mContent = ""
    private fun deleteFields() {
        inputProduct.text.clear()
        inputBrand.text.clear()
        inputBarcode.text.clear()
        inputPrice.text.clear()
        rootCl.requestFocus()
    }

    private fun onInputReceived(list: ArrayList<Int>) {
        var content = ""
        list.forEach {
            if (it > 32) {
                val itemChar = it.toChar()
                Log.d(TAG, "item --> $itemChar")
                content += itemChar
            }
        }
        Log.d(TAG, "barcodeId --> $content")
        activity?.let {
            val mainActivity = it as? MainActivity

            when {
                content.startsWith(Constants.URL_SCHEMA) ->
                    //It's a QRCode
                    //Try to find coupon by Id and then apply discount in current transaction
                    // so communicate to activity the coupon code and then It will communicate to the Fragment to search for QrCode
                    mainActivity?.searchForQrCode(content)
                content.startsWith(ConfigurationManager.getInstance(it).storeId.plus("-")) -> {
                    //It's a Coupon Barcode
                    mainActivity?.searchForBarcodeCoupon(content)
                }
                else -> {
                    mContent = content
                    mProductsViewModel.getItemByBarcodeId(content)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_insert_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deleteFields()
        activity?.let {
            if (it is MainActivity) {
                it.setInsertFragmentInterface(this)
            }
        }
        mProductsViewModel = ViewModelProviders.of(this).get(ProductsViewModel::class.java)
        mTransactionsViewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)

        mProductsViewModel.getItemByBarcode().observe(this, Observer {
            Log.d(TAG, "ITEM --> $it")
            if (it != null) {
                if (inputBarcode.hasFocus() || inputProduct.hasFocus() || inputBrand.hasFocus() || inputPrice.hasFocus()) {
                    inputBarcode.setText(it.barcodeId)
                    inputProduct.setText(it.product)
                    inputBrand.setText(it.brand)
                    inputPrice.setText(it.price.toString())
                } else {
                    mTransactionsViewModel.insertItem(it)
                }
            } else {
                if (!inputBarcode.hasFocus()) {
                    inputBarcode.setText(mContent)
                } else {
                    //Note that when an EditText has Focus, only the Tab Terminator char will be received from scanner.
                    // So we get text from EditText directly
                    mProductsViewModel.getItemByBarcodeId(inputBarcode.text.toString())
                }
            }
        })

        inputPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isEmpty()) {
                        Utils.expandOrCollapseView(fabDeletePrice, 300, 0)
                    } else {
                        val viewHeight = Utils.measureViewHeight(fabDeletePrice)
                        Utils.expandOrCollapseView(fabDeletePrice, 300, viewHeight)
                    }
                }

                if (inputPrice == null || s.toString().isEmpty()) return
                inputPrice.removeTextChangedListener(this)
                try {
                    val inputString = s.toString()
                    val cleanString = inputString.replace("[\\s€$,.]".toRegex(), "")
                    val bigDecimal = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
                    val converted = NumberFormat.getCurrencyInstance(Locale.ITALY).format(bigDecimal)
                    inputPrice.setText(converted)
                    inputPrice.setSelection(converted.length)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    inputPrice.addTextChangedListener(this)
                }
                inputPrice.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        fabDeletePrice.setOnClickListener{
            inputPrice.setText("")
        }

        add.setOnClickListener {
            if (!inputProduct.text.toString().trim().equals("") && !inputBrand.text.toString().trim().equals("") && !inputBarcode.text.toString().trim().equals("") && !inputPrice.text.toString().trim().equals("")) {
                val item = Item("", inputProduct.text.toString().trim(), inputBrand.text.toString().trim(), "", inputBarcode.text.toString().trim(), inputPrice.text.toString().trim().replace("[\\s€$,.]".toRegex(), "").toInt())
                mProductsViewModel.insert(item)
                deleteFields()
            } else {
                Toast.makeText(activity, getString(R.string.must_fill_all_fields), Toast.LENGTH_LONG).show()
            }
        }

        deleteAll.setOnClickListener {
            activity?.let { fragmentActivity: FragmentActivity ->
                val builder = AlertDialog.Builder(fragmentActivity)
                builder.setTitle(R.string.delete_all_products_dialog_title)
                builder.setMessage(R.string.delete_all_inputs_dialog_message)
                builder.setPositiveButton(R.string.yes) { dialog, which ->
                    deleteFields()
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

    override fun onIntReceived(int: Int) {
        if (int == 9) {
            Log.d(TAG, "Sending to listener")
            onInputReceived(mInputList)
            mInputList.clear()
        } else {
            Log.d(TAG, "Adding char")
            mInputList.add(int)
        }
    }

    override fun setQrCodeImage(image: Bitmap) {
        ivQrCode.setImageBitmap(image)
        llQrCode.visibility = View.VISIBLE
    }
}