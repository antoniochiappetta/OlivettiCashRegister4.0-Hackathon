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
package it.tim.innovation.jolmilano.cr40devapp.room

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import it.tim.innovation.jolmilano.cr40devapp.model.Item
import it.tim.innovation.jolmilano.cr40devapp.model.ReceiptLine

/**
 * Created by Gaetano Dati on 05/07/2019
 */
class ReceiptRepository(private val receiptDao: ReceiptDao) {

    val allItems: LiveData<List<ReceiptLine>> = receiptDao.getAllItems()

    @WorkerThread
    suspend fun insert(item: ReceiptLine){
        receiptDao.insertItem(item)
    }

    @WorkerThread
    suspend fun deleteItem(item: ReceiptLine){
        receiptDao.deleteItem(item)
    }

    @WorkerThread
    suspend fun deleteAllItems(){
        receiptDao.deleteAll()
    }
}