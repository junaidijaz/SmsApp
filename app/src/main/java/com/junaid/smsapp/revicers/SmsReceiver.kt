package com.junaid.smsapp.revicers

import android.app.PendingIntent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.junaid.smsapp.App.Companion.CHANNEL_1_ID
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.ui.ComposeActivity
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

                Log.d("SmsReceiver", "senderNum: $phoneNumber; message: $message")
            } // end for loop

            if (!isActive)
                NotificationHelper.sendChannel1Notification(phoneNumber, message, p0!!)

            SmsContract.putSmsToInboxDatabase(message, phoneNumber, p0!!)
            onSmsReceived?.onSmsReceived()
        }
    }

//    private fun sendOnChannel1(phoneNo: String, sms: String, context: Context) {
//
//
//        val notificationManager = NotificationManagerCompat.from(context)
//        val contactName = SmsContract.getContactName(phoneNo, context)
//
//        val activityIntent = Intent(context, ComposeActivity::class.java)
//        activityIntent.putExtra("notificationAddress", phoneNo)
//        val contentIntent = PendingIntent.getActivity(
//            context,
//            0, activityIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//
//        val remoteInput = RemoteInput.Builder("key_text_reply")
//            .setLabel("Your Answer")
//            .build()
//
//        val replyIntent = Intent(context, DirectReplyReceiver::class.java)
//        val replyPendingIntent = PendingIntent.getBroadcast(context, 44, replyIntent, 0)
//
//        val replyAction = NotificationCompat.Action.Builder(
//            R.drawable.ic_send_black_24dp,
//            "Reply",
//            replyPendingIntent
//        ).addRemoteInput(remoteInput).build()
//
//
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notification = NotificationCompat.Builder(context, CHANNEL_1_ID)
//            .setSmallIcon(R.drawable.ic_action_message)
//            .setStyle(
//                NotificationCompat.InboxStyle()
//                    .setBigContentTitle("New Sms from ${contactName ?: phoneNo}")
//                    .addLine(sms)
//            )
//            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
//            .setSound(defaultSoundUri)
//            .setAutoCancel(true)
//            .setContentIntent(contentIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//            .build()
//
//
//
//
//        if (!isActive) {
//            notificationManager.notify(1, notification)
//        }
//
//
//    }

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