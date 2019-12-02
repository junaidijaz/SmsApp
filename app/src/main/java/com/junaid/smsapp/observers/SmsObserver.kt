package com.junaid.smsapp.observers

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.Telephony


class SmsObserver(context: Context, address: String, body: String) : ContentObserver(handler) {

    companion object {
        private val handler = Handler()
        private val uri = Uri.parse("content://sms/sent")
    }

    private val context: Context
    private val resolver: ContentResolver?
    private val address: String
    private val body: String

    interface OnSmsSentListener {
        fun onSmsSent(threadId: String,smsId : String )
    }

    init {

        if (context is OnSmsSentListener) {
            this.context = context
            this.resolver = context.contentResolver
            this.address = address
            this.body = body
        } else {
            throw IllegalArgumentException(
                "Context must implement OnSmsSentListener interface"
            )
        }
    }

    fun start() {
        if (resolver != null) {
            resolver.registerContentObserver(uri, true, this)

        } else {
            throw IllegalStateException(
                "Current SmsObserver instance is invalid"
            )
        }
    }

   override fun onChange(selfChange: Boolean, uri: Uri) {
        var cursor: Cursor? = null

        try {
            cursor = resolver!!.query(uri, null, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val type = cursor.getInt(
                    cursor.getColumnIndex(Telephony.Sms.TYPE)
                )

                if (type == Telephony.Sms.Sent.MESSAGE_TYPE_SENT) {
                    var address = cursor.getString(
                        cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                    )
                    val body = cursor.getString(
                        cursor.getColumnIndex(Telephony.Sms.BODY)
                    )
                    val threadId = cursor.getString(
                        cursor.getColumnIndex(Telephony.Sms.THREAD_ID)
                    )

                    val smsId = cursor.getString(
                        cursor.getColumnIndex(Telephony.Sms._ID)
                    )
                    if(address.contains("+92")) {
                        address = address.replace("+92", "0")
                    }
                    if(address.contains(" "))
                        address = address.replace(" ","")

//                                && body == this.body
                    if (address == this.address ) {

                        (context as OnSmsSentListener).onSmsSent(threadId,smsId)
                        resolver.unregisterContentObserver(this)
                    }
                }
            }
        } finally {
            cursor?.close()
        }
    }


}