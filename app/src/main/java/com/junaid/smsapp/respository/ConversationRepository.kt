package com.junaid.smsapp.respository

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.utils.MyPreference
import com.junaid.smsapp.utils.SmsContract

class ConversationRepository(private val conversationDao: ConversationDao) {



    var getAllBlockedConversations = conversationDao.getAllBlockedConversations(true)
    var spamConversations = conversationDao.getAllSpamConversations()
    var unReadSms = conversationDao.getUnreadSms()
    var pinnedSms = conversationDao.getPinnedSms()
    var readSms = conversationDao.getReadSms()
    var conversationDeleted = MutableLiveData<Int>()

    suspend fun insertConversationList(convoList: ArrayList<Conversation>, context: Context) {
        conversationDao.insertAllConversation(LinkedHashSet(convoList))
        MyPreference.getInstance(context)?.saveData("syncingFirstTime","saved")
        UpdateNameAsync(conversationDao,context).execute(convoList)
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

    }

    suspend fun blockAddress(flag: Boolean, phoneNo: String) {
        conversationDao.blockAddress(flag, phoneNo)
    }

    fun pinSms(flag: Boolean, phoneNo: String) {
        conversationDao.markSmsPinned(flag, phoneNo)
    }

    fun getContactName(address: String) =
        conversationDao.getContactName(address)


    fun getThreadId(phoneNo: String) = conversationDao.getThreadId(phoneNo)

    fun getAllConversation(filter: String?): LiveData<List<Conversation>> {
        return conversationDao.getAllConversation(filter)
    }


    class UpdateNameAsync internal constructor(private val dao: ConversationDao, var context: Context) :
        AsyncTask<List<Conversation>, Int, Void>() {
        override fun doInBackground(vararg conversation: List<Conversation>): Void? {
//            dao.insertConversation(conversation[0])
            for(item in conversation[0])
            {
              val ad=  dao.getConversation(item.address)[0].contactName
                if(ad.isNullOrEmpty() || ad ==  "" )
                dao.updateName(
                    SmsContract.getContactName(item.address, context) ?: item.address,
                    item.address
                )
            }


            return null
        }
    }


}