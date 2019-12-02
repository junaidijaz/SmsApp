package com.junaid.smsapp.respository

import android.content.Context
import com.junaid.smsapp.model.room.ConversationDao
import com.junaid.smsapp.utils.SmsContract

class ComposeRepository(private val conversationDao: ConversationDao) {

    suspend fun changeConversationReadState(context: Context, readState: String, threadId: String) {
        conversationDao.changeConversationReadState(readState, threadId)
        SmsContract.markMessageRead(context, threadId)

    }


    suspend fun getContactName(address : String) = conversationDao.getContactName(address)
}