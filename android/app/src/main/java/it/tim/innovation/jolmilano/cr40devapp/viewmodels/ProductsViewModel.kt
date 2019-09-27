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
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.room.ItemRepository
import it.tim.innovation.jolmilano.cr40devapp.room.ItemRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val mItemRepository: ItemRepository
    private val mAllItems: LiveData<List<Item>>
    private val mScope = CoroutineScope(Dispatchers.IO)
    private val mItemByBarcode = MediatorLiveData<Item>()

    init {
        val itemsDao = ItemRoomDatabase.getDatabase(application).itemDao()
        mItemRepository = ItemRepository(itemsDao)
        mAllItems = mItemRepository.allItems
    }

    fun getAllItems(): LiveData<List<Item>> = mAllItems

    fun getItemByBarcode(): LiveData<Item> = mItemByBarcode

    fun insert(item: Item) = mScope.launch {
        mItemRepository.insert(item)
    }

    fun deleteAllItems() = mScope.launch {
        mItemRepository.deleteAllItems()
    }

    fun deleteItem(item: Item) = mScope.launch {
        mItemRepository.deleteItem(item)
    }

    fun getItemByBarcodeId(barcodeId: String) = mScope.launch {
        mItemByBarcode.postValue(mItemRepository.getItemByBarcodeId(barcodeId))
    }
}