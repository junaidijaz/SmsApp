package com.junaid.smsapp.ui

import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SmsMessage
import androidx.appcompat.app.AppCompatActivity
import com.junaid.smsapp.App
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ComposeChatAdapter
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.observers.SmsObserver
import com.junaid.smsapp.revicers.DeliverReceiver
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import com.junaid.smsapp.utils.SmsContract.Companion.BODY
import com.junaid.smsapp.utils.SmsContract.Companion.CONTACTNAME
import com.junaid.smsapp.utils.SmsContract.Companion.DATE
import com.junaid.smsapp.utils.SmsContract.Companion.INBOX_SMS_URI
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_IS_NOT_READ
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_IS_NOT_SEEN
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_TYPE_INBOX
import com.junaid.smsapp.utils.SmsContract.Companion.MESSAGE_TYPE_SENT
import com.junaid.smsapp.utils.SmsContract.Companion.READ
import com.junaid.smsapp.utils.SmsContract.Companion.SEEN
import com.junaid.smsapp.utils.SmsContract.Companion.SENT_SMS_URI
import com.junaid.smsapp.utils.SmsContract.Companion.THREADID
import com.junaid.smsapp.utils.SmsContract.Companion.TYPE
import kotlinx.android.synthetic.main.compose_activity.*


class ComposeActivity : AppCompatActivity(), SmsObserver.OnSmsSentListener {


    var contactName: String? = null
    var threadId: String? = null
    var smsList = ArrayList<Conversation>()

    companion object{
       var isActive = false
    }

    private lateinit var smsSentReceiver: BroadcastReceiver
    private lateinit var smsReceiver: BroadcastReceiver
    private var deliveryBroadcastReceiver = DeliverReceiver()


    lateinit var conversationAdapter: ComposeChatAdapter

    private var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)

        address = intent.getStringExtra(ADDRESS)!!
        contactName = intent.getStringExtra(CONTACTNAME)
        threadId = intent.getStringExtra(THREADID)

        toolbar.title = contactName ?: address
        toolbar.subtitle = if (contactName != null) address else ""
        smsList = ArrayList(getSmsForContact(threadId))
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

//        registerReceiver(smsSentReceiver, IntentFilter("SENT"))
//        registerReceiver(deliveryBroadcastReceiver, IntentFilter("SMS_DELIVERED"))


        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                getSmsFromReceiver(bundle)
            }
        }

        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))


    }

    fun getSmsFromReceiver(bundle: Bundle?) {
        val pdusObj = bundle?.get("pdus") as Array<Any>?

        for (i in pdusObj!!.indices) {

            val currentMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray)
            val phoneNumber = currentMessage.displayOriginatingAddress
            val message = currentMessage.displayMessageBody
            putSmsToDatabase(message, INBOX_SMS_URI, phoneNumber)


        } // end for loop

    }

    override fun onSmsSent(threadId: String, smsId: String) {

    }

    private fun refreshConversation() {
        smsList.clear()
        smsList.addAll(getSmsForContact(threadId))
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

        putSmsToDatabase(message, SENT_SMS_URI, null)


    }

    private fun putSmsToDatabase(sms: String, uri: Uri, _address: String?) {
        // Create SMS row
        val contentResolver = this.contentResolver
        val values = ContentValues()
        values.put(ADDRESS, _address ?: address)
        values.put(DATE, System.currentTimeMillis())
        values.put(READ, MESSAGE_IS_NOT_READ)

        if (uri == SENT_SMS_URI)
            values.put(TYPE, MESSAGE_TYPE_SENT)
        else
            values.put(TYPE, MESSAGE_TYPE_INBOX)

        values.put(SEEN, MESSAGE_IS_NOT_SEEN)
        values.put(BODY, sms)
        // Push row into the SMS table
        contentResolver.insert(uri, values)

        refreshConversation()

    }

    override fun onResume() {
        super.onResume()
      isActive = true
    }

    private fun getSmsForContact(threadId: String?): List<Conversation> {

        val lstSms = ArrayList<Conversation>()

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

//        unregisterReceiver(smsSentReceiver)
//        unregisterReceiver(deliveryBroadcastReceiver)
        unregisterReceiver(smsReceiver)
        isActive  = false
        super.onPause()
    }

}
