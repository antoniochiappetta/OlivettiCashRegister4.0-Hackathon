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

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import it.tim.innovation.jolmilano.cr40devapp.model.QrCodeItem
import java.security.NoSuchAlgorithmException


/**
 * Created by Gaetano Dati on 06/07/2019
 */
class Utils {
    companion object {

        fun showSnackBar(view: View, message: String) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }

        fun showDialog(context: Context, title: String, message: String) {
            AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setCancelable(true)
                    .create()
                    .show()
        }

        fun hideSoftKeyboard(fragmentActivity: FragmentActivity) {
            val imm = fragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var focusedView = fragmentActivity.currentFocus
            if (focusedView == null) {
                focusedView = View(fragmentActivity)
            }
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        }

        private fun getParentWidth(viewToScale: View): Int {
            val viewParent = viewToScale.parent

            if (viewParent is View) {
                val width = viewParent.width
                if (width > 0) {
                    return width
                }
            }

            return 0
        }

        fun measureViewHeight(view: View): Int {
            val availableWidth = getParentWidth(view)
            val widthMeasureSpec =
                    if (availableWidth > 0) {
                        View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.AT_MOST)
                    } else {
                        View.MeasureSpec.UNSPECIFIED
                    }
            val heightMeasureSpec = View.MeasureSpec.UNSPECIFIED
            view.measure(widthMeasureSpec, heightMeasureSpec)
            return view.measuredHeight
        }

        fun expandOrCollapseView(v: View, duration: Int, targetHeight: Int) {

            val prevHeight = v.height

            v.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }

        fun rotateFabForward(view: View) {
            ViewCompat.animate(view)
                    .rotation(135.0f)
                    .withLayer()
                    .setDuration(300L)
                    .setInterpolator(OvershootInterpolator(10.0f))
                    .start()
        }

        fun rotateFabBackward(view: View) {
            ViewCompat.animate(view)
                    .rotation(0.0f)
                    .withLayer()
                    .setDuration(300L)
                    .setInterpolator(OvershootInterpolator(10.0f))
                    .start()
        }

        fun getMd5(s: String): String {
            val MD5 = "MD5"
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest
                        .getInstance(MD5)
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2)
                        h = "0$h"
                    hexString.append(h)
                }
                return hexString.toString()

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }

        fun generateCouponId(ctx: Context, timestampCreated: Long, discountPercent: Int): String{
            return getMd5(ConfigurationManager.getInstance(ctx).storeId.plus(timestampCreated).plus(discountPercent))
        }

        fun buildUrl(ctx: Context, item: QrCodeItem): String{

            val builder = Uri.Builder()
            builder
                    .scheme(Constants.URL_SCHEMA)
                    .authority(Constants.URL_AUTHORITY)
                    .appendPath(Constants.OLIVETTI_PATH)
                    .appendPath(Constants.STORES_PATH)
                    .appendPath(ConfigurationManager.getInstance(ctx).storeId)
                    .appendPath(Constants.COUPONS_PATH)
                    .appendPath(item.couponId)
                    .appendQueryParameter(Constants.TITLE_PARAM, item.title)
                    .appendQueryParameter(Constants.DESCRIPTION_PARAM, item.description)
                    .appendQueryParameter(Constants.TYPE_PARAM, item.discountTYpe.toString())
                    .appendQueryParameter(Constants.DISCOUNT_PERCENT_PARAM, item.discountPercent.toString())
                    .appendQueryParameter(Constants.EXPIRE_PARAM, item.timestampExpiration.toString())
                    .appendQueryParameter(Constants.IMG_URL_PARAM, item.imgUrl)

            return builder.build().toString()
        }
    }
}