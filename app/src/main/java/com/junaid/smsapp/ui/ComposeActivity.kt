package com.junaid.smsapp.ui

import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ComposeChatAdapter
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.observers.SmsObserver
import com.junaid.smsapp.revicers.OnSmsReceived
import com.junaid.smsapp.revicers.SmsReceiver
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import com.junaid.smsapp.utils.SmsContract.Companion.BODY
import com.junaid.smsapp.utils.SmsContract.Companion.CONTACTNAME
import com.junaid.smsapp.utils.SmsContract.Companion.DATE
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


class ComposeActivity : AppCompatActivity(), SmsObserver.OnSmsSentListener, OnSmsReceived {

    override fun onSmsReceived() {
        refreshConversation()
    }


    var contactName: String? = null
    var threadId: String? = null
    var smsList = ArrayList<Conversation>()
    var fromNotification = false

    companion object {
        var isActive = false
    }


    lateinit var conversationAdapter: ComposeChatAdapter
    private var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)


        getContactInfo()


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
                SmsContract.sendMySMS(sms,address,this)
                refreshConversation()
            }
        }


    }

    private fun getContactInfo() {

        if (intent.getStringExtra("notificationAddress") != null) {
            fromNotification = true
            address = intent.getStringExtra("notificationAddress") ?: ""
            threadId = SmsContract.getThreadId(address,this)
            NotificationHelper.removeNotification(this,threadId)
            Log.d("TAG", "getContactInfo: $threadId")
            contactName = SmsContract.getContactName(address, this)
            return
        }
        address = intent.getStringExtra(ADDRESS)!!
        contactName = intent.getStringExtra(CONTACTNAME)
        threadId = intent.getStringExtra(THREADID)
        Log.d("TAG", "getContactInfo: $threadId")

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


    }

    override fun onResume() {
        super.onResume()
        refreshConversation()
        SmsReceiver.setOnSmsLisenter(this)
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
        SmsReceiver.setOnSmsLisenter(null)
        isActive = false
        super.onPause()
    }

}
