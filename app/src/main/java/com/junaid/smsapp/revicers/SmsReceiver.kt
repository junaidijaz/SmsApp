package com.junaid.smsapp.revicers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.junaid.smsapp.App.Companion.CHANNEL_1_ID
import com.junaid.smsapp.R
import com.junaid.smsapp.ui.ComposeActivity.Companion.isActive
import com.junaid.smsapp.utils.SmsContract


class SmsReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context?, intent: Intent?) {
        // Get Bundle object contained in the SMS intent passed in
        val bundle = intent?.extras

        if (bundle != null && intent.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val pdusObj = bundle.get("pdus") as Array<Any>?

            for (i in pdusObj!!.indices) {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                var message = ""
                for (sms in smsMessages) {
                    message += sms.displayMessageBody
                }
                val phoneNumber = smsMessages[0].displayOriginatingAddress
                sendOnChannel1(phoneNumber, message, p0!!)
                Log.d("SmsReceiver", "senderNum: $phoneNumber; message: $message")

            } // end for loop

        }
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

    private fun sendOnChannel1(phoneNo: String, sms: String, context: Context) {

        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_1_ID)
            .setSmallIcon(R.drawable.ic_action_message)
            .setContentTitle("New Sms from $phoneNo")
            .setContentText(sms)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()




        if (!isActive) {
            notificationManager.notify(1, notification)
            SmsContract.putSmsToInboxDatabase(sms, phoneNo, context)
            onSmsReceived?.onSmsReceived()
        }
    }


}