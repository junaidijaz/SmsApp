package com.junaid.smsapp.ui

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AppCompatActivity
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ComposeChatAdapter
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.revicers.DeliverReceiver
import com.junaid.smsapp.revicers.SentReceiver
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import com.junaid.smsapp.utils.SmsContract.Companion.BODY
import com.junaid.smsapp.utils.SmsContract.Companion.CONTACTNAME
import com.junaid.smsapp.utils.SmsContract.Companion.DATE
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_IS_NOT_READ
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_IS_NOT_SEEN
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_TYPE_SENT
import com.junaid.smsapp.utils.SmsContract.Companion.READ
import com.junaid.smsapp.utils.SmsContract.Companion.SEEN
import com.junaid.smsapp.utils.SmsContract.Companion.SENT_SMS_URI
import com.junaid.smsapp.utils.SmsContract.Companion.THREADID
import com.junaid.smsapp.utils.SmsContract.Companion.TYPE
import kotlinx.android.synthetic.main.compose_activity.*


class ComposeActivity : AppCompatActivity() {

    lateinit var conversationAdapter: ComposeChatAdapter
    var contactName: String? = null
    var threadId: String? = null
    var smsList = ArrayList<Conversation>()

    var smsSentReceiver = SentReceiver()
    private var deliveryBroadcastReceiver = DeliverReceiver()

    private var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)

        address = intent.getStringExtra(ADDRESS)!!
        contactName = intent.getStringExtra(CONTACTNAME)
        threadId = intent.getStringExtra(THREADID)

        toolbar.title = contactName ?: address
        toolbar.subtitle = if (contactName != null) address else ""
        smsList = ArrayList(getSmsForContact(address))
        buildRecyclerView()
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        send.setOnClickListener {
            val sms = message.text.toString().trim()
            if (sms.isNotEmpty()) {
                message.text.clear()
                sendMySMS(sms)
            }
        }

        registerReceiver(smsSentReceiver, IntentFilter("SENT"))
        registerReceiver(deliveryBroadcastReceiver, IntentFilter("SMS_DELIVERED"))


    }

    private fun refreshConversation() {
        smsList.clear()
        smsList.addAll(getSmsForContact(address))
        conversationAdapter.notifyDataSetChanged()
        conversationAdapter.notifyItemInserted(smsList.size - 1)
        messageList.scrollToPosition(smsList.size - 1)
    }

    private fun buildRecyclerView() {
        conversationAdapter = ComposeChatAdapter(smsList)
        messageList.adapter = conversationAdapter
    }

    private fun sendMySMS(message: String) {


        val sms = SmsManager.getDefault()

        // if message length is too long messages are divided
        val messages = sms.divideMessage(message)
        for (msg in messages) {

            val sentIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
            val deliveredIntent =
                PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), 0)
            sms.sendTextMessage(address, null, msg, sentIntent, deliveredIntent)
        }



        putSmsToDatabase(message)
        refreshConversation()

    }

    private fun putSmsToDatabase(sms: String) {
        // Create SMS row
        val contentResolver = this.contentResolver
        val values = ContentValues()
        values.put(ADDRESS, address)
        values.put(DATE, System.currentTimeMillis())
        values.put(READ, MESSAGE_IS_NOT_READ)
//        values.put(STATUS, )
        values.put(TYPE, MESSAGE_TYPE_SENT)
        values.put(SEEN, MESSAGE_IS_NOT_SEEN)
        values.put(BODY, sms)
        // Push row into the SMS table
        contentResolver.insert(SENT_SMS_URI, values)

    }


    private fun getSmsForContact(contact: String): List<Conversation> {



        val lstSms = ArrayList<Conversation>()
        val selectionArgs = arrayOf(contact)
        val cr = this.contentResolver
        val c = cr.query(
            SmsContract.ALL_SMS_URI,
            null,
            "thread_id=$threadId",
            null,
            SmsContract.SORT_ASC
        )

        val totalSMS = c!!.count

        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val objSms = Conversation()
                objSms.id = c.getString(c.getColumnIndexOrThrow("_id"))
                //number of conversation
                objSms.address = c.getString(c.getColumnIndexOrThrow("address"))
//                objSms.contactName = getContactName(objSms.address!!,this)
                objSms.msg = c.getString(c.getColumnIndexOrThrow("body"))
                //1 if msg is read
                objSms.readState = c.getString(c.getColumnIndex("read"))
                objSms.time = c.getString(c.getColumnIndexOrThrow("date"))

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.folderName = "inbox"
                } else {
                    objSms.folderName = "sent"
                }

                lstSms.add(objSms)
                c.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }

        c.close()

        return lstSms
    }

    override fun onPause() {

        unregisterReceiver(smsSentReceiver)
        unregisterReceiver(deliveryBroadcastReceiver)
        super.onPause()
    }

}
