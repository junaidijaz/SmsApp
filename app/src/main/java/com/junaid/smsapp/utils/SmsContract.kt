package com.junaid.smsapp.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri

/**
 * Created by R Ankit on 25-12-2016.
 */

class SmsContract {

    companion object {
        val SMS_EXTRA_NAME = "pdus"
        val SMS_URI = "content://sms"

        val ADDRESS = "address"
        val CONTACTNAME = "contactName"
        val PERSON = "person"
        val DATE = "date"
        val READ = "read"
        val STATUS = "status"
        val TYPE = "type"
        val BODY = "body"
        val SEEN = "seen"
        val THREADID = "thread_id"

        val MESSAGE_TYPE_INBOX = 1
        val MESSAGE_TYPE_SENT = 2

        val MESSAGE_IS_NOT_READ = 0
        val MESSAGE_IS_READ = 1

        val MESSAGE_IS_NOT_SEEN = 0
        val MESSAGE_IS_SEEN = 1


        val ALL_SMS_URI = Uri.parse("content://sms")
        val INBOX_SMS_URI = Uri.parse("content://sms/inbox")
        val SENT_SMS_URI = Uri.parse("content://sms/sent")
        val SMS_SELECTION = "address = ? "
        val SMS_SELECTION_ID = "_id = ? "
        val COLUMN_ID = "_id"
        val SMS_SELECTION_SEARCH = "address LIKE ? OR body LIKE ?"
        val SORT_DESC = "date DESC"
        val SORT_ASC = "date ASC"



          fun putSmsToInboxDatabase(sms: String, _address: String?, context: Context) {
            // Create SMS row
            val contentResolver = context.contentResolver
            val values = ContentValues()
            values.put(ADDRESS, _address)
            values.put(DATE, System.currentTimeMillis())
            values.put(READ, MESSAGE_IS_NOT_READ)
            values.put(TYPE, MESSAGE_TYPE_INBOX)
            values.put(SEEN, MESSAGE_IS_NOT_SEEN)
            values.put(BODY, sms)
            // Push row into the SMS table
            contentResolver.insert(INBOX_SMS_URI, values)
        }


    }



}
