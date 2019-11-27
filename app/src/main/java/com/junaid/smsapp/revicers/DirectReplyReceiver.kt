package com.junaid.smsapp.revicers

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.NotificationHelper.Companion.KEY_REPLY
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.utils.SmsContract


class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val bundle = intent?.extras
            val cDao = ConversationRoomDatabase.getDatabase(context!!).conversationDao()

            val replyText = remoteInput.getCharSequence(KEY_REPLY) as String
            val phoneNumber = bundle?.getString("notificationAddress") as String
            val contactName = cDao.getContactName(phoneNumber)

            NotificationHelper.sendChannel1Notification(contactName,phoneNumber, replyText, context)
            val ti = SmsContract.getThreadId(phoneNumber, context)
            Log.d("TAG", "onReceive DirectReply: $phoneNumber  $ti")
            SmsContract.sendMySMS(replyText, phoneNumber, context)
        }
    }
}