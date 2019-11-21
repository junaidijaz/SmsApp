package com.junaid.smsapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.junaid.smsapp.App.Companion.CHANNEL_1_ID
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.revicers.DirectReplyReceiver
import com.junaid.smsapp.ui.ComposeActivity
import com.junaid.smsapp.utils.SmsContract

class NotificationHelper {


    companion object {

       private var notificationMessages: Multimap<Int, Conversation> = ArrayListMultimap.create()
        const val KEY_REPLY = "key_reply"

        fun sendChannel1Notification(phoneNo: String, sms: String, context: Context) {

            val activityIntent = Intent(context, ComposeActivity::class.java)
            activityIntent.putExtra("notificationAddress", phoneNo)
            val contentIntent = PendingIntent.getActivity(
                context,
                0, activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val threadId = SmsContract.getThreadId(phoneNo, context)!!.toInt()
            Log.d("TAG", "onReceive DirectReply: $phoneNo  $threadId")
            notificationMessages.put(
                threadId,
                Conversation(address = phoneNo, msg = sms)
            )

            val remoteInput = RemoteInput.Builder(KEY_REPLY)
                .setLabel("Your answer...")
                .build()

            val replyIntent: Intent
            var replyPendingIntent: PendingIntent? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                replyIntent = Intent(context, DirectReplyReceiver::class.java)
                replyIntent.putExtra("notificationAddress", phoneNo)
                replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                //cancel notification with notificationManagerCompat.cancel(id)
            }

            val replyAction = NotificationCompat.Action.Builder(
                R.drawable.ic_send_black_24dp,
                "Reply",
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()


            val messagingStyle = NotificationCompat.MessagingStyle("Me")
            messagingStyle.conversationTitle =
                SmsContract.getContactName(phoneNo, context) ?: phoneNo

            for (chatMessage in notificationMessages.get(threadId)) {
                val notificationMessage = NotificationCompat.MessagingStyle.Message(
                    chatMessage.msg,
                    System.currentTimeMillis(),
                    chatMessage.address
                )
                messagingStyle.addMessage(notificationMessage)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setStyle(messagingStyle)
                .addAction(replyAction)
                .setColor(Color.BLUE)
                .setSmallIcon(R.drawable.ic_action_message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(
                SmsContract.getThreadId(phoneNo, context)!!.toInt(),
                notification
            )
        }

        fun removeNotification(context: Context, threadId: String?) {
            if (threadId != null) {
                val notificationManager = NotificationManagerCompat.from(context)

                if (notificationMessages.containsKey(threadId))
                    notificationMessages.removeAll(threadId)

                notificationManager.cancel(threadId.toInt())

            }
        }





    }
}