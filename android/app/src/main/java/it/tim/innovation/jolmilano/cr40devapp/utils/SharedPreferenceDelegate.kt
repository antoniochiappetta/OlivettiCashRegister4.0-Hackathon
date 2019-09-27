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
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import android.content.SharedPreferences.Editor
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Created by manuela on 01/03/18.
 */
class SharedPreferenceDelegate<T>(
        private val context: Context,
        private val defaultValue: T,
        private val getter: SharedPreferences.(String, T) -> T,
        private val setter: Editor.(String, T) -> Editor,
        private val key: String
) : ReadWriteProperty<Any, T> {

    private val safeContext: Context by lazyFast { context.safeContext() }

    private val sharedPreferences: SharedPreferences by lazyFast {
        PreferenceManager.getDefaultSharedPreferences(safeContext)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) =
            sharedPreferences
                    .getter(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
            sharedPreferences
                    .edit()
                    .setter(key, value)
                    .apply()
}

@Suppress("UNCHECKED_CAST")
fun <T> bindSharedPreference(context: Context, key: String, defaultValue: T): ReadWriteProperty<Any, T> =
        when (defaultValue) {
            is Boolean ->
                SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getBoolean, Editor::putBoolean, key)
            is Int ->
                SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getInt, Editor::putInt, key)
            is Long ->
                SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getLong, Editor::putLong, key)
            is Float ->
                SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getFloat, Editor::putFloat, key)
            is String ->
                SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getString, Editor::putString, key)
            else -> throw IllegalArgumentException()
        } as ReadWriteProperty<Any, T>