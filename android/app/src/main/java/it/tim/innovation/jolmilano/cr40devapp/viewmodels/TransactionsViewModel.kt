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
package it.tim.innovation.jolmilano.cr40devapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.model.QrCodeItem
import it.tim.innovation.jolmilano.cr40devapp.model.ReceiptLine
import it.tim.innovation.jolmilano.cr40devapp.room.ItemRoomDatabase
import it.tim.innovation.jolmilano.cr40devapp.room.QrCodeRepository
import it.tim.innovation.jolmilano.cr40devapp.room.ReceiptRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val mScope = CoroutineScope(Dispatchers.IO)

    private val mQrCodeRepository: QrCodeRepository
    private val mReceiptRepository: ReceiptRepository

    private val mAllItems: LiveData<List<ReceiptLine>>
    private val mItemByCouponId = MediatorLiveData<QrCodeItem>()
    private val mIsMenuExpanded = MutableLiveData<Boolean>()
    private val mIsPromo20Switched = MutableLiveData<Boolean>()
    private val mIsPromo40Switched = MutableLiveData<Boolean>()

    init {
        mIsMenuExpanded.value = true
        mIsPromo20Switched.value = false
        mIsPromo40Switched.value = false
        val receiptDao = ItemRoomDatabase.getDatabase(application).receiptDao()
        mReceiptRepository = ReceiptRepository(receiptDao)
        val qrCodeDao = ItemRoomDatabase.getDatabase(application).qrCodeDao()
        mQrCodeRepository = QrCodeRepository(qrCodeDao)
        mAllItems = mReceiptRepository.allItems
    }

    fun getItemByCouponId(): LiveData<QrCodeItem> = mItemByCouponId

    fun getIsPromo20Switched(): LiveData<Boolean> = mIsPromo20Switched

    fun getIsPromo40Switched(): LiveData<Boolean> = mIsPromo40Switched

    fun getIsMenuExpanded(): LiveData<Boolean> = mIsMenuExpanded

    fun setIsPromo20Switched(isPromoSwitched: Boolean) {
        mIsPromo20Switched.value = isPromoSwitched
    }

    fun setIsMenuExpanded(isMenuExpanded: Boolean) {
        mIsMenuExpanded.value = isMenuExpanded
    }

    fun setIsPromo40Switched(isPromoSwitched: Boolean) {
        mIsPromo40Switched.value = isPromoSwitched
    }

    fun getAllItems(): LiveData<List<ReceiptLine>> = mAllItems

    fun insertQrCode(qrCodeItem: QrCodeItem) = mScope.launch {
        mQrCodeRepository.insertQrCode(qrCodeItem)
    }

    fun insertItem(item: Item) = mScope.launch {
        val receiptItem = ReceiptLine(item.barcodeId, 1, item.product, item.brand, item.price, item.price)
        mReceiptRepository.insert(receiptItem)
    }

    fun deleteAllItems() = mScope.launch {
        mReceiptRepository.deleteAllItems()
    }

    fun deleteItem(item: ReceiptLine) = mScope.launch {
        mReceiptRepository.deleteItem(item)
    }

    fun getItemByCouponId(couponId: String) = GlobalScope.launch {
        mItemByCouponId.postValue(mQrCodeRepository.getItemCouponId(couponId))
    }
}