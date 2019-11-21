package com.junaid.smsapp.utils

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationRoomDatabase

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


        fun putSmsToInboxDatabase(
            sms: String,
            _address: String?,
            isSpam: Boolean,
            context: Context
        ) {
            // Create SMS row
            val cDao = ConversationRoomDatabase.getDatabase(context).conversationDao()

            val threadId = cDao.getThreadId(_address!!)
            val conversation = Conversation(
                address = _address,
                msg = sms,
                folderName = "inbox",
                isSpam = isSpam,
                readState = "0",
                threadId = threadId,
                time = System.currentTimeMillis().toString()
            )
            cDao.insertConversation(conversation)

            if (isSpam)
                return

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

        private fun putSmsToSentDatabase(sms: String, _address: String?, context: Context) {
            // Create SMS row
            val contentResolver = context.contentResolver
            val values = ContentValues()
            values.put(ADDRESS, _address)
            values.put(DATE, System.currentTimeMillis())
            values.put(READ, MESSAGE_IS_READ)
            values.put(TYPE, MESSAGE_TYPE_SENT)
            values.put(SEEN, MESSAGE_IS_SEEN)
            values.put(BODY, sms)
            // Push row into the SMS table
            contentResolver.insert(INBOX_SMS_URI, values)
        }

        fun getContactName(phoneNumber: String?, context: Context): String? {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )

            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            var contactName = null as String?
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0)
                }
                cursor.close()
            }

            return contactName
        }


        fun sendMySMS(message: String, phoneNumber: String, context: Context) {
            val sms = SmsManager.getDefault()
            // if message length is too long messages are divided
            val messages = sms.divideMessage(message)
            for (msg in messages) {

                val sentIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), 0)
                val deliveredIntent =
                    PendingIntent.getBroadcast(context, 0, Intent("SMS_DELIVERED"), 0)
                sms.sendTextMessage(phoneNumber, null, msg, sentIntent, deliveredIntent)
            }
            putSmsToSentDatabase(message, phoneNumber, context)
        }


        fun getThreadId(number: String, context: Context): String? {

            val contentResolver = context.contentResolver
            val uri = Uri.parse("content://sms/")

            val cursor = contentResolver.query(
                uri,
                null,
                "thread_id IS NOT NULL) GROUP BY (thread_id AND address=?",
                arrayOf(number),
                "date DESC"
            )

            var threadId = ""


            while (cursor!!.moveToNext()) {
                threadId = cursor.getString(cursor.getColumnIndex("thread_id"))
            }
            cursor.close()
            return threadId
        }


    }


}
