package com.junaid.smsapp.respository

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.utils.SmsContract

class ConversationRepository(private val conversationDao: ConversationDao) {


    var allConversations = conversationDao.getAllConversation(false)
    var getAllBlockedConversations = conversationDao.getAllBlockedConversations(true)
    var spamConversations = conversationDao.getAllSpamConversations()
    var conversationDeleted = MutableLiveData<Int>()
    var readSms = conversationDao.getReadSms()
    var unReadSms = conversationDao.getUnreadSms()
    var pinnedSms = conversationDao.getPinnedSms()

    suspend fun insertConversationList(convoList: ArrayList<Conversation>, context: Context) {

        conversationDao.insertAllConversation(convoList)
        updateNameAsync(conversationDao,context).execute(convoList)

    }


    suspend fun spamAddress(isSpam: Boolean, address: String) {
        conversationDao.setSpamAddress(isSpam, address)
    }

    fun getSpamConversation() {
        conversationDao.getAllSpamConversations()
    }

    fun insertConversation(conversation: Conversation) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun deleteAllConversation() {
        conversationDao.deleteAll()
    }

    suspend fun deleteConversation(threadId: String) {
        conversationDao.deleteConversation(threadId)
//        deleteSMS()
    }

    suspend fun blockAddress(flag: Boolean, phoneNo: String) {
        conversationDao.blockAddress(flag, phoneNo)
    }

    fun pinSms(flag: Boolean, phoneNo: String) {
        conversationDao.markSmsPinned(flag, phoneNo)
    }

    fun getThreadId(phoneNo: String) = conversationDao.getThreadId(phoneNo)


    class updateNameAsync internal constructor(private val dao: ConversationDao,var context: Context) :
        AsyncTask<List<Conversation>, Int, Void>() {
        override fun doInBackground(vararg conversation: List<Conversation>): Void? {
//            dao.insertConversation(conversation[0])
            for(item in conversation[0])
            {
                dao.updateName(
                    SmsContract.getContactName(item.address, context) ?: item.address,
                    item.address
                )
            }


            return null
        }
    }


}