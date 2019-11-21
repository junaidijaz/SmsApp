package com.junaid.smsapp.respository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao

class ConversationRepository(private val conversationDao: ConversationDao) {


    var allConversations = conversationDao.getAllConversation(false)
    var getAllBlockedConversations = conversationDao.getAllBlockedConversations(true)
    var conversationDeleted = MutableLiveData<Int>()

    suspend fun insertConversationList(convoList: ArrayList<Conversation>) {
        conversationDao.insertAllConversation(convoList)
    }

    suspend fun spamAddress(isSpam: Boolean, address: String) {
        conversationDao.setSpamAddress(isSpam, address)
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

    fun getThreadId(phoneNo: String) = conversationDao.getThreadId(phoneNo)


//    class insertConversationAsyncTask internal constructor(private val dao: ConversationDao) :
//        AsyncTask<Conversation, Int, Void>() {
//        override fun doInBackground(vararg conversation: Conversation): Void? {
//            dao.insertConversation(conversation[0])
//            return null
//        }
//    }

    fun deleteSMS(context: Context, number: String) {
        try {

            val uriSms = Uri.parse("content://sms/inbox")
            val c = context.contentResolver.query(
                uriSms,
                arrayOf("_id", "thread_id", "address", "person", "date", "body"), null, null, null
            )

            if (c != null && c.moveToFirst()) {
                do {
                    val id = c.getLong(0)
                    val address = c.getString(2)
                    if (address == number) {

                        context.contentResolver.delete(
                            Uri.parse("content://sms/$id"), null, null
                        )
                    }
                } while (c.moveToNext())
            }
            c?.close()
        } catch (e: Exception) {

        } finally {
        }


    }
}