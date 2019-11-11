package com.junaid.smsapp.revicers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        // Get Bundle object contained in the SMS intent passed in
        val bundle = intent?.extras

        if (bundle != null) {
            val pdusObj = bundle.get("pdus") as Array<Any>?

            for (i in pdusObj!!.indices) {

                val currentMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray)
                saveSmsInInbox(p0, currentMessage)
                val phoneNumber = currentMessage.displayOriginatingAddress

                val message = currentMessage.displayMessageBody

                Log.i("SmsReceiver", "senderNum: $phoneNumber; message: $message")


                // Show alert
                val duration = Toast.LENGTH_LONG
                val toast =
                    Toast.makeText(p0, "senderNum: $phoneNumber, message: $message", duration)
                toast.show()

            } // end for loop

        }
    }

    private fun saveSmsInInbox(context: Context?, sms: SmsMessage) {
        val serviceIntent = Intent(context, SmsReceiver::class.java)
        serviceIntent.putExtra("sender_no", sms.displayOriginatingAddress)
        serviceIntent.putExtra("message", sms.displayMessageBody)
        serviceIntent.putExtra("date", sms.timestampMillis)
        context?.startService(serviceIntent)

    }

}