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

package it.tim.innovation.jolmilano.cr40devapp.presentation

import android.app.Presentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Display
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import it.tim.innovation.jolmilano.cr40devapp.R
import it.tim.innovation.jolmilano.cr40devapp.utils.Actions
import kotlinx.android.synthetic.main.presentation_layout.*

class TransactionPresentation(var outerContext: Context?, display: Display?) : Presentation(outerContext, display) {

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Actions.ACTION_UPDATE_MESSAGE -> {
                    intent.extras?.let {
                        message.text = it.getString(Actions.EXTRA_MESSAGE)

                        productDescription.visibility = View.GONE
                        totalCount.visibility = View.GONE
                        totalDiscount.visibility = View.GONE
                        message.visibility = View.VISIBLE
                    }
                }
                Actions.ACTION_UPDATE_PRODUCT -> {

                    intent.extras?.let { bundle: Bundle ->
                        outerContext?.let { outerContext: Context ->

                            val prodName = bundle.getString(Actions.EXTRA_PRODUCT_NAME)
                            val prodPrice = bundle.getDouble(Actions.EXTRA_PRODUCT_PRICE)
                            val discountPercent = bundle.getDouble(Actions.EXTRA_DISCOUNT_PERCENT)
                            val discount = bundle.getDouble(Actions.EXTRA_TOTAL_DISCOUNT)
                            val total = bundle.getDouble(Actions.EXTRA_PRODUCT_TOTAL)

                            productDescription.text = String.format(outerContext.getString(R.string.presentation_product), prodName, prodPrice)

                            if (discountPercent > 0.0) {
                                totalDiscount.text = String.format(outerContext.getString(R.string.discount_applied_percent), discountPercent, discount)
                                totalDiscount.visibility = View.VISIBLE
                            } else {
                                totalDiscount.visibility = View.GONE
                            }

                            totalCount.text = String.format(outerContext.getString(R.string.presentation_total), total)

                            message.visibility = View.GONE
                            totalCount.visibility = View.VISIBLE
                            productDescription.visibility = if (prodName != null) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }

                        }
                    }
                }
                Actions.ACTION_EMPTY_LIST -> {
                    outerContext?.let {
                        message.text = it.getString(R.string.welcome)

                        message.visibility = View.VISIBLE
                        productDescription.visibility = View.GONE
                        totalDiscount.visibility = View.GONE
                        totalCount.visibility = View.GONE
                    }
                }

                Actions.ACTION_DISCOUNT_DELETED -> {
                    intent.extras?.let { bundle: Bundle ->

                        outerContext?.let { outerContext: Context ->
                            totalDiscount.text = String.format(outerContext.getString(R.string.discount_applied_percent), 0.00, 0.00)
                            totalDiscount.visibility = View.GONE

                            val total = bundle.getDouble(Actions.EXTRA_PRODUCT_TOTAL)
                            totalCount.text = String.format(outerContext.getString(R.string.presentation_total), total)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentation_layout)
    }

    override fun onStart() {
        super.onStart()
        outerContext?.let {
            val filter = IntentFilter()
            filter.addAction(Actions.ACTION_UPDATE_PRODUCT)
            filter.addAction(Actions.ACTION_UPDATE_MESSAGE)
            filter.addAction(Actions.ACTION_EMPTY_LIST)
            filter.addAction(Actions.ACTION_DISCOUNT_DELETED)
            LocalBroadcastManager.getInstance(it).registerReceiver(mReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        outerContext?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(mReceiver)
        }
    }

}

