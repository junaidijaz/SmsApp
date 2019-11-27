package com.junaid.smsapp.revicers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.provider.Telephony
import android.util.Log
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.ui.ComposeActivity.Companion.isActive
import com.junaid.smsapp.utils.SmsContract
import java.util.*
import kotlin.concurrent.schedule


class SmsReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context?, intent: Intent?) {
        // Get Bundle object contained in the SMS intent passed in
        val bundle = intent?.extras

        val cDao = ConversationRoomDatabase.getDatabase(p0!!).conversationDao()

        if (bundle != null && intent.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val pdusObj = bundle.get("pdus") as Array<Any>?

            var message = ""
            var phoneNumber = ""
            var contactName = ""

            for (i in pdusObj!!.indices) {
                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in smsMessages) {
                    message += sms.displayMessageBody
                }

                phoneNumber = smsMessages[0].displayOriginatingAddress

                if (phoneNumber.contains("+92")) {
                    phoneNumber = phoneNumber.replace("+92", "0")
                }
                contactName = cDao.getContactName(phoneNumber) ?: ""
            } // end for loop
            when (numberIsSpamOrBlock(cDao,phoneNumber, p0)) {
                "blocked" -> {
                    return
                }
                "spam" -> {
                    SmsContract.putSmsToInboxDatabase(contactName,message, phoneNumber, true, p0)
                    return
                }
                else -> {
                    SmsContract.putSmsToInboxDatabase(contactName,message, phoneNumber, false, p0)
                    if (!isActive)
                        NotificationHelper.sendChannel1Notification(
                            contactName,
                            phoneNumber,
                            message,
                            p0
                        )
                }


            }

            Handler().postDelayed({
                onSmsReceived?.onSmsReceived()
            }, 1000)


        }
    }

    private fun numberIsSpamOrBlock(cDao : ConversationDao,phoneNo: String, context: Context): String {



        val conversation = cDao.getBlockedAddressCount(true, address = phoneNo)

        if (conversation > 0)
            return "blocked"

        val count = cDao.getSpamConversationsCount(address = phoneNo)
        if (count > 0)
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