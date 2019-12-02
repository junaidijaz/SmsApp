package com.junaid.smsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.AutoCompleteAdapter
import com.junaid.smsapp.adapters.ComposeChatAdapter
import com.junaid.smsapp.model.ContactAddress
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.observers.SmsObserver
import com.junaid.smsapp.revicers.OnSmsReceived
import com.junaid.smsapp.revicers.SmsReceiver
import com.junaid.smsapp.ui.viewmodel.ComposeViewModel
import com.junaid.smsapp.ui.viewmodel.ContactsViewModel
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import kotlinx.android.synthetic.main.compose_activity.*


class ComposeActivity : AppCompatActivity(), SmsObserver.OnSmsSentListener, OnSmsReceived {

    override fun onSmsReceived() {
        refreshConversation()
    }

    private lateinit var conversationAdapter: ComposeChatAdapter
    private lateinit var composeViewModel: ComposeViewModel
    lateinit var contactsViewModel: ContactsViewModel

    lateinit var cDao: ConversationDao
    var contactName: String? = null
    var threadId: String = ""
    var smsList = ArrayList<Conversation>()
    var chattedRecipents = ArrayList<String>()
    var sms = ""

    var fromNotification = false

    companion object {
        var isActive = false
    }

    private var address = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        cDao = ConversationRoomDatabase.getDatabase(this).conversationDao()
        composeViewModel = ViewModelProvider(this).get(ComposeViewModel::class.java)
        contactsViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)


        getContactInfoFromIntent()

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        send.setOnClickListener {
            sms = message.text.toString().trim()
            if (sms.isNotEmpty()) {
                message.text.clear()
                if (chattedRecipents.size > 0) {
                    SmsObserver(this, address, sms).start()
                    val conversation = Conversation(msg = sms,address = address)
                    SmsContract.putNewConversationInSentFolder(conversation,this,false)
                } else {
                    SmsContract.sendMySMS(sms, address, this)
                    refreshConversation()
                }
            }
        }
    }

    private fun getContactInfoFromIntent() {

        if (intent.getStringExtra("notificationAddress") != null) {
            fromNotification = true
            address = intent.getStringExtra("notificationAddress") ?: ""
            threadId = cDao.getThreadId(address)!!
            NotificationHelper.removeNotification(this, threadId)
            Log.d("TAG", "getContactInfo: $threadId")
            contactName = cDao.getContactName(address)
            setToolbar()
            getSmsForContact(threadId)
            return
        } else if (intent.getStringExtra("fromFab") != null) {
            //new sms
            toolbar.title = "Compose"
            chattedRecipents = intent.getStringArrayListExtra("recipients")!!
            val recipients = contactsViewModel.getAllContacts() as ArrayList<ContactAddress>
            setAutoCompleteTextView(recipients)
            return
        }
        address = intent.getStringExtra(ADDRESS)!!
        contactName = cDao.getContactName(address)
        threadId = cDao.getThreadId(address)!!
        setToolbar()
        getSmsForContact(threadId)
        changeConversationReadState(SmsContract.MESSAGE_IS_READ.toString(), threadId)

    }

    private fun setAutoCompleteTextView(mRecipients: ArrayList<ContactAddress>) {
        etAutoComplete.visibility = View.VISIBLE
        val adapter = AutoCompleteAdapter(this, mRecipients)
        etAutoComplete.setAdapter(adapter)
        etAutoComplete.setOnItemClickListener { adapterView, view, i, l ->
            val selectedContact = adapterView.getItemAtPosition(i) as ContactAddress
            if (selectedContact.address.contains("+92")) {
                selectedContact.address = selectedContact.address.replace("+92", "0")
            }
            selectedContact.address = selectedContact.address.replace(" ", "")
            address = selectedContact.address
            contactName = selectedContact.name


            if (chattedRecipents.contains(selectedContact.address)) {
                etAutoComplete.visibility = View.GONE
                threadId = cDao.getThreadId(address)!!
                getSmsForContact(threadId)
                setToolbar()
            } else {

            }


        }
    }

    private fun setToolbar() {
        toolbar.title = contactName ?: address
        toolbar.subtitle = if (contactName != null) address else ""
    }

    private fun changeConversationReadState(readState: String, threadId: String) {
        composeViewModel.changeConversationReadState(readState, threadId)
    }


    override fun onSmsSent(threadId: String, smsId: String) {
        this.threadId = threadId
        val convo = Conversation(
                address = address,
                contactName = contactName,
                msg = sms,
                threadId = threadId
            )
        etAutoComplete.visibility = View.GONE
        SmsContract.putNewConversationInSentFolder(convo, this,true)
        setToolbar()
        refreshConversation()
    }

    private fun refreshConversation() {

        getSmsForContact(threadId)
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
        SmsReceiver.setOnSmsLisenter(this)
        isActive = true
    }

    private fun getSmsForContact(threadId: String?) {
        smsList.clear()
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

        smsList.clear()
        smsList.addAll(lstSms)
        buildRecyclerView()
    }


    override fun onPause() {
        SmsReceiver.setOnSmsLisenter(null)
        isActive = false
        super.onPause()
    }

}
