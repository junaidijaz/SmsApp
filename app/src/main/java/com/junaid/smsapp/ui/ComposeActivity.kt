package com.junaid.smsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ComposeChatAdapter
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.observers.SmsObserver
import com.junaid.smsapp.revicers.OnSmsReceived
import com.junaid.smsapp.revicers.SmsReceiver
import com.junaid.smsapp.ui.viewmodel.ComposeViewModel
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import kotlinx.android.synthetic.main.compose_activity.*


class ComposeActivity : AppCompatActivity(), SmsObserver.OnSmsSentListener, OnSmsReceived {

    override fun onSmsReceived() {
        refreshConversation()
    }

    lateinit var composeViewModel: ComposeViewModel

    lateinit var cDao: ConversationDao

    var contactName: String? = null
    var threadId: String = ""
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
        cDao = ConversationRoomDatabase.getDatabase(this).conversationDao()
        composeViewModel = ViewModelProvider(this).get(ComposeViewModel::class.java)


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
                SmsContract.sendMySMS(sms, address, this)
                refreshConversation()
            }
        }
    }

    private fun getContactInfo() {

        if (intent.getStringExtra("notificationAddress") != null) {
            fromNotification = true
            address = intent.getStringExtra("notificationAddress") ?: ""
            threadId = cDao.getThreadId(address)
            NotificationHelper.removeNotification(this, threadId)
            Log.d("TAG", "getContactInfo: $threadId")
            contactName = cDao.getContactName(address)
            return
        }
        address = intent.getStringExtra(ADDRESS)!!
        contactName = cDao.getContactName(address)
        threadId = cDao.getThreadId(address)

        changeConversationReadState(SmsContract.MESSAGE_IS_READ.toString(), threadId)

    }

    private fun changeConversationReadState(readState: String, threadId: String) {
        composeViewModel.changeConversationReadState(readState, threadId)
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
//                objSms.address = c.getString(c.getColumnIndexOrThrow("address")) ?: ""
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
