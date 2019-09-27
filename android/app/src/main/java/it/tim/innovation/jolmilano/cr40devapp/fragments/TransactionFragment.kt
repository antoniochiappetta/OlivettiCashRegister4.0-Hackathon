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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import it.tim.innovation.jolmilano.cr40devapp.MainActivity
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.model.QrCodeItem
import it.tim.innovation.jolmilano.cr40devapp.model.ReceiptLine
import it.tim.innovation.jolmilano.cr40devapp.utils.*
import it.tim.innovation.jolmilano.cr40devapp.viewmodels.TransactionsViewModel
import kotlinx.android.synthetic.main.fragment_transactions.*
import java.net.URL

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class TransactionFragment : Fragment(), OperationsInterface {

    private val TAG = TransactionFragment::class.java.name

    private lateinit var mTransactionsViewModel: TransactionsViewModel
    private lateinit var mListener: FiscalReceipt
    private lateinit var mListAdapter: ReceiptAdapter
    private var mTotal: Int = 0
    private var mDiscountPercent: Int = 0
    private var mDiscountTotal: Int = 0
    private var mIsFabMenuExpanded = true
    private var mIsPromo20Switched = false
    private var mIsPromo40Switched = false
    private var mQrCodeItemFound: QrCodeItem? = null

    private fun clearDiscount(printTotal: Boolean){
        mTotal += mDiscountTotal
        mDiscountTotal = 0
        mDiscountPercent = 0
        vatPercent.visibility = View.GONE
        if(printTotal){
            totalTv.text = String.format(getString(R.string.receipt_total), mTotal.div(Constants.RECEIPT_ROUND))
        }
    }

    private fun applyDiscount(discountPercent: Int){
        activity?.let { fragmentActivity: FragmentActivity ->
            mTotal += mDiscountTotal

            mDiscountPercent = discountPercent

            mDiscountTotal = (mTotal * mDiscountPercent) / 100
            mTotal -= mDiscountTotal
            totalTv.text = String.format(getString(R.string.receipt_total), mTotal.div(Constants.RECEIPT_ROUND))

            if (mDiscountPercent > 0) {
                vatPercent.text = String.format(getString(R.string.discount_applied_percent), mDiscountPercent.times(Constants.RECEIPT_DISCOUNT_MULTIPLIER), mDiscountTotal.div(Constants.RECEIPT_ROUND))
                vatPercent.visibility = View.VISIBLE
            } else {
                vatPercent.visibility = View.GONE
            }

            //Send broadcast to Second display
            val intent = Intent()
            intent.action = Actions.ACTION_UPDATE_PRODUCT
            intent.putExtra(Actions.EXTRA_DISCOUNT_PERCENT, mDiscountPercent.times(Constants.RECEIPT_DISCOUNT_MULTIPLIER))
            intent.putExtra(Actions.EXTRA_TOTAL_DISCOUNT, mDiscountTotal.div(Constants.RECEIPT_ROUND))
            intent.putExtra(Actions.EXTRA_PRODUCT_TOTAL, mTotal.div(Constants.RECEIPT_ROUND))
            LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FiscalReceipt) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + "must implement FiscalReceipt")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { fragmentActivity: FragmentActivity ->

            if (fragmentActivity is MainActivity) fragmentActivity.setOperationListener(this)
            mTransactionsViewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
            mListAdapter = ReceiptAdapter(fragmentActivity)
            mListAdapter.setOnItemActionListener(object : ReceiptAdapter.OnItemActionListener {
                override fun onItemClick(item: ReceiptLine) {

                }

                override fun onItemLongClick(item: ReceiptLine) {

                }
            })

            productsList.adapter = mListAdapter
            productsList.layoutManager = LinearLayoutManager(fragmentActivity)

            mTransactionsViewModel.getAllItems().observe(this, Observer {
                if (it.isNotEmpty()) {
                    mTotal = 0
                    it.forEach { listItem ->
                        mTotal += listItem.price
                    }

                    mDiscountTotal = (mTotal * mDiscountPercent) / 100
                    mTotal -= mDiscountTotal
                    if (mDiscountPercent > 0) {
                        vatPercent.text = String.format(getString(R.string.discount_applied_percent), mDiscountPercent.times(Constants.RECEIPT_DISCOUNT_MULTIPLIER), mDiscountTotal.div(Constants.RECEIPT_ROUND))
                        vatPercent.visibility = View.VISIBLE
                    } else {
                        vatPercent.visibility = View.GONE
                    }
                    mListAdapter.setItems(it)
                    noItemsTv.visibility = View.GONE
                    productsList.visibility = View.VISIBLE
                    productsList.scrollToPosition(it.size - 1)

                    //Send broadcast to Second display
                    val intent = Intent()
                    intent.action = Actions.ACTION_UPDATE_PRODUCT
                    intent.putExtra(Actions.EXTRA_PRODUCT_NAME, it[it.size - 1].prodName)
                    intent.putExtra(Actions.EXTRA_PRODUCT_PRICE, it[it.size - 1].price.div(Constants.RECEIPT_ROUND))
                    intent.putExtra(Actions.EXTRA_DISCOUNT_PERCENT, mDiscountPercent.times(Constants.RECEIPT_DISCOUNT_MULTIPLIER))
                    intent.putExtra(Actions.EXTRA_TOTAL_DISCOUNT, mDiscountTotal.div(Constants.RECEIPT_ROUND))
                    intent.putExtra(Actions.EXTRA_PRODUCT_TOTAL, mTotal.div(Constants.RECEIPT_ROUND))
                    LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)
                } else {
                    clearDiscount(false)
                    mTotal = 0
                    noItemsTv.visibility = View.VISIBLE
                    mListAdapter.setItems(it)
                    productsList.visibility = View.GONE

                    //Send broadcast to Second display
                    val intent = Intent()
                    intent.action = Actions.ACTION_EMPTY_LIST
                    LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)
                }
                totalTv.text = String.format(getString(R.string.receipt_total), mTotal.div(Constants.RECEIPT_ROUND))
            })

            mTransactionsViewModel.getIsMenuExpanded().observe(this, Observer { isMenuExpanded: Boolean? ->
                isMenuExpanded?.let { isExpanded: Boolean ->
                    if (isExpanded) {
                        Utils.expandOrCollapseView(llFabMenu, 300, 0)
                    } else {
                        val viewHeight = Utils.measureViewHeight(llFabMenu)
                        Utils.expandOrCollapseView(llFabMenu, 300, viewHeight)
                    }
                }
            })

            mTransactionsViewModel.getIsPromo20Switched().observe(this, Observer {
                Log.d(TAG,"isPromo20Switched --> $it")
                mIsPromo20Switched = it
                if(it && mIsPromo40Switched){
                    switchPromo40.isChecked = false
                }
            })

            mTransactionsViewModel.getIsPromo40Switched().observe(this, Observer {
                Log.d(TAG,"isPromo40Switched --> $it")
                mIsPromo40Switched = it
                if(it && mIsPromo20Switched){
                    switchPromo20.isChecked = false
                }
            })

            mTransactionsViewModel.getItemByCouponId().observe(this, Observer { qrCodeItem: QrCodeItem? ->
                if(qrCodeItem == null){
                    //Show popup for QrCode not found
                    Utils.showDialog(fragmentActivity, getString(R.string.qr_code_not_found_title), getString(R.string.qr_code_not_found_message))
                }else{
                    //Proceed with discount check
                    Log.d(TAG,"qrCodeItem found -> $qrCodeItem")
                    if(qrCodeItem.timestampUsed > 0){
                        //Coupon has already been used
                        Utils.showDialog(fragmentActivity, getString(R.string.qr_code_already_used_title), getString(R.string.qr_code_already_used_message))
                    }else{
                        //Discount has never been used, let's check if it's expired
                        val expired = (qrCodeItem.timestampExpiration - qrCodeItem.timestampCreated) < 0
                        if(expired){
                            //Discount is Expired
                            Utils.showDialog(fragmentActivity, getString(R.string.qr_code_expired_title), getString(R.string.qr_code_expired_message))
                        }else{
                            //Discount is not expired, now apply discount and then update qrCodeItem so that next time it will be invalid
                            applyDiscount(qrCodeItem.discountPercent)
                            this.mQrCodeItemFound = qrCodeItem
                        }
                    }
                }
            })

            switchPromo20.setOnCheckedChangeListener {_, active ->
                mTransactionsViewModel.setIsPromo20Switched(active)
            }

            switchPromo40.setOnCheckedChangeListener{_, active ->
                mTransactionsViewModel.setIsPromo40Switched(active)
            }

            /*actionFab.setOnClickListener {
                if (mIsFabMenuExpanded) {
                    Utils.rotateFabForward(actionFab)
                } else {
                    Utils.rotateFabBackward(actionFab)
                }
                mIsFabMenuExpanded = !mIsFabMenuExpanded
                mTransactionsViewModel.setIsMenuExpanded(mIsFabMenuExpanded)
            }*/

            /*llAddDiscount.setOnClickListener {
                activity?.let {
                    val builder = AlertDialog.Builder(it).setTitle(getString(R.string.add_discount_on_total))
                    val inflater = it.layoutInflater
                    val dialogView = inflater.inflate(R.layout.add_total_vat_layout, null)
                    builder.setView(dialogView)

                    val vatPercentEt = dialogView.findViewById<EditText>(R.id.vatPercent)

                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->

                        mTotal += mDiscountTotal

                        mDiscountPercent =
                                (if (vatPercentEt.text.isNotEmpty()) {
                                    vatPercentEt.text.toString().toDoubleOrNull()
                                } else {
                                    0.0
                                }) as Double

                        mDiscountTotal = (mTotal * mDiscountPercent) / 100
                        mTotal -= mDiscountTotal
                        totalTv.text = String.format(getString(R.string.receipt_total), mTotal)

                        if (mDiscountPercent > 0.0) {
                            vatPercent.text = String.format(getString(R.string.discount_applied_percent), mDiscountPercent, mDiscountTotal)
                            vatPercent.visibility = View.VISIBLE
                        } else {
                            vatPercent.visibility = View.GONE
                        }

                        //Send broadcast to Second display
                        val intent = Intent()
                        intent.action = Actions.ACTION_UPDATE_PRODUCT
                        intent.putExtra(Actions.EXTRA_DISCOUNT_PERCENT, mDiscountPercent)
                        intent.putExtra(Actions.EXTRA_TOTAL_DISCOUNT, mDiscountTotal)
                        intent.putExtra(Actions.EXTRA_PRODUCT_TOTAL, mTotal)
                        LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)

                        val imm = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        var view = it.currentFocus
                        if (view == null) {
                            view = View(it)
                        }
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                        dialog.dismiss()
                    }
                    builder.create().show()
                }
            }*/

            deleteAll.setOnClickListener {
                val builder = AlertDialog.Builder(fragmentActivity)
                builder.setTitle(R.string.delete_all_products_dialog_title)
                builder.setMessage(R.string.delete_all_products_dialog_message)
                builder.setPositiveButton(R.string.yes) { dialog, _ ->
                    mTransactionsViewModel.deleteAllItems()
                    Utils.hideSoftKeyboard(fragmentActivity)
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            deleteDiscount.setOnClickListener {
                val builder = AlertDialog.Builder(fragmentActivity)
                builder.setTitle(R.string.delete_discount_dialog_title)
                builder.setMessage(R.string.delete_discount_dialog_message)
                builder.setPositiveButton(R.string.yes) { dialog, _ ->
                    clearDiscount(true)

                    //Send broadcast to Second display
                    val intent = Intent()
                    intent.action = Actions.ACTION_DISCOUNT_DELETED
                    intent.putExtra(Actions.EXTRA_PRODUCT_TOTAL, mTotal.div(Constants.RECEIPT_ROUND))
                    LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)

                    Utils.hideSoftKeyboard(fragmentActivity)
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            printReceiptButton.setOnClickListener {
                mListener.onPrintFiscalReceipt(mListAdapter.getItems(), mDiscountTotal, mDiscountPercent, mIsPromo20Switched, mIsPromo40Switched)

                mQrCodeItemFound?.let {
                    it.timestampUsed = System.currentTimeMillis()
                    mTransactionsViewModel.insertQrCode(it)
                    mQrCodeItemFound = null
                }

                //Send broadcast to second screen
                val intent = Intent()
                intent.action = Actions.ACTION_UPDATE_MESSAGE
                intent.putExtra(Actions.EXTRA_MESSAGE, "Thanks!")
                LocalBroadcastManager.getInstance(fragmentActivity).sendBroadcast(intent)
            }
        }
    }

    override fun onOperationFinished() {
        frameProgress.visibility = View.GONE
        mTransactionsViewModel.deleteAllItems()
    }

    override fun onOperationOngoing() {
        frameProgress.visibility = View.VISIBLE
        frameProgress.setOnClickListener { }
    }

    override fun searchForQrCodeCoupon(qrCode: String) {
        val url = URL(qrCode)
        val splitUrl = url.path.split("/")
        Log.d(TAG,"splitUrl --> $splitUrl")
        val storeId = splitUrl[splitUrl.size - 3]
        val couponId = splitUrl[splitUrl.size - 1]
        if(couponId.equals(Constants.TEST_QR_CODE)){
            //That's for test purpose, we assume that it exists in db
            Log.d(TAG,"It's a test QrCode")
            applyDiscount(20)
        }else{
            activity?.let{
                //Check whether storeID is VALID
                if(storeId.equals(ConfigurationManager.getInstance(it.applicationContext).storeId)){
                    mTransactionsViewModel.getItemByCouponId(couponId)
                }else{
                    Utils.showDialog(it, getString(R.string.qr_code_store_error_title), getString(R.string.qr_code_store_error_message))
                }
            }
        }
    }

    override fun searchForBarcodeCoupon(barcode: String) {
        Log.d(TAG,"Coupon Barcode --> $barcode")
        activity?.let {
            val storeId = ConfigurationManager.getInstance(it).storeId
            if(storeId.equals(barcode.substring(0,3))){
                Log.d(TAG,"StoreID OK")
                //We're surely looking for a discount which has been printed out from our Shop
                mTransactionsViewModel.getItemByCouponId(barcode.substringAfter("-"))
            }else{
                Utils.showDialog(it, getString(R.string.qr_code_store_error_title), getString(R.string.qr_code_store_error_message))
            }
        }
    }

    interface FiscalReceipt {
        /**
         * Called when a normal receipt has to be printed
         * @param list The list to pass
         */
        fun onPrintFiscalReceipt(list: List<ReceiptLine>, discountTotal: Int, discountPercent: Int, isPromo20Switched: Boolean, isPromo40Switched: Boolean)
    }
}