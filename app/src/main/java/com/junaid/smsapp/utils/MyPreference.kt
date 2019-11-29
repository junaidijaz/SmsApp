package com.junaid.smsapp.utils

import android.content.Context
import android.content.SharedPreferences

class MyPreference private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences? = context.getSharedPreferences(
        "YourCustomNamedPreference",
        Context.MODE_PRIVATE
    )

    fun saveData(key: String?, value: String?) {
        val prefsEditor = sharedPreferences!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun getData(key: String?): String? {
        return if (sharedPreferences != null) {
            sharedPreferences.getString(key, "")
        } else ""
    }

    companion object {
        private var yourPreference: MyPreference? = null
        fun getInstance(context: Context): MyPreference? {
            if (yourPreference == null) {
                yourPreference = MyPreference(context)
            }
            return yourPreference
        }
    }

}