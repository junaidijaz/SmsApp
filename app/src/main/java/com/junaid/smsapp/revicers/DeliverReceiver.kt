package com.junaid.smsapp.revicers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telecom.TelecomManager
import android.widget.Toast





class DeliverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        if(intent.action.equals("android.provider.Telephony.SMS_DELIVERED")) {
            when (resultCode) {
                Activity.RESULT_OK ->
                    Toast.makeText(
                        context, "SMS Delivered",
                        Toast.LENGTH_SHORT
                    ).show()
                Activity.RESULT_CANCELED -> Toast.makeText(
                    context, "SMS not delivered",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
