package com.junaid.smsapp.revicers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.ui.ComposeActivity.Companion.isActive
import com.junaid.smsapp.utils.SmsContract


class SmsReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context?, intent: Intent?) {
        // Get Bundle object contained in the SMS intent passed in
        val bundle = intent?.extras

        if (bundle != null && intent.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val pdusObj = bundle.get("pdus") as Array<Any>?

            var message = ""
            var phoneNumber = ""
            for (i in pdusObj!!.indices) {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in smsMessages) {
                    message += sms.displayMessageBody
                }
                phoneNumber = smsMessages[0].displayOriginatingAddress

                if (phoneNumber.contains("+92")) {
                    phoneNumber = phoneNumber.replace("+92", "0")
                }

                Log.d("SmsReceiver", "senderNum: $phoneNumber; message: $message")
            } // end for loop


            when (numberIsSpamOrBlock(phoneNumber, p0!!)) {
                "blocked" -> {
                    return
                }
                "spam" -> {
                    SmsContract.putSmsToInboxDatabase(message, phoneNumber, true, p0)
                    return
                }
                else -> {

                    SmsContract.putSmsToInboxDatabase(message, phoneNumber, false, p0)
                    if (!isActive)
                        NotificationHelper.sendChannel1Notification(
                            phoneNumber,
                            message,
                            p0
                        )

                }


            }
            onSmsReceived?.onSmsReceived()
        }
    }

    private fun numberIsSpamOrBlock(phoneNo: String, context: Context): String {

        val cDao = ConversationRoomDatabase.getDatabase(context).conversationDao()

        var conversation = cDao.getBlockConversation(true, address = phoneNo)

        if (conversation.isNotEmpty())
            return "blocked"

        conversation = cDao.getSpamConversations(true, address = phoneNo)
        if (conversation.isNotEmpty())
            return "spam"

        return "good"

    }


    companion object {

        private var onSmsReceived: OnSmsReceived? = null

        fun setOnSmsLisenter(onSmsReceived: OnSmsReceived?) {
            this.onSmsReceived = onSmsReceived
        }

        fun removeSmsLisenter() {
            this.onSmsReceived = null
        }
    }


}