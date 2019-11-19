package com.junaid.smsapp.respository

import androidx.lifecycle.MutableLiveData
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationDao
import java.text.FieldPosition

class ConversationRepository(private val conversationDao: ConversationDao) {


     var allConversations = conversationDao.getAllConversation()
     var conversationDeleted = MutableLiveData<Int>()

    suspend fun insertConversationList(convoList: ArrayList<Conversation>) {
        conversationDao.insertAllConversation(convoList)
    }

   suspend fun insertConversation(convoList: Conversation) {
        conversationDao.insertConversation(convoList)
    }

    suspend fun deleteAllConversation()
    {
        conversationDao.deleteAll()
    }

    suspend fun deleteConversation(threadId: String, position: Int)
    {
        conversationDao.deleteConversation(threadId)
    }


}