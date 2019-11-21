package com.junaid.smsapp.model.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.junaid.smsapp.model.Conversation

@Dao
interface ConversationDao {


    @Query("SELECT * from Conversation where isSpam = :isSpam and isBlocked = :isSpam ORDER BY time DESC")
    fun getAllConversation(isSpam: Boolean): LiveData<List<Conversation>>

    @Query("SELECT * from Conversation where address = :phoneNo")
    fun getConversation(phoneNo: String): List<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllConversation(conversation: List<Conversation>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertConversation(conversation: Conversation)

    @Query("DELETE FROM Conversation")
    suspend fun deleteAll()

    @Query("DELETE FROM Conversation where threadId = :threadId")
    suspend fun deleteConversation(threadId: String)

    @Query("SELECT threadId FROM Conversation where address = :address")
    fun getThreadId(address: String): String

    @Query("Update conversation set isBlocked = :flag where address = :address")
    suspend fun blockAddress(flag: Boolean, address: String)

    @Query("SELECT * FROM Conversation where isBlocked = :isBlocked and address = :address")
    fun getBlockConversation(isBlocked: Boolean, address: String) : List<Conversation>

     @Query("SELECT * FROM Conversation where isBlocked = :isBlocked")
    fun getAllBlockedConversations(isBlocked: Boolean) : LiveData<List<Conversation>>

    @Query("Update conversation set isSpam = :isSpam where address = :address")
    suspend fun setSpamAddress(isSpam: Boolean, address: String)
    
    @Query("SELECT * FROM Conversation where isSpam = :isSpam and address = :address")
    fun getSpamConversations(isSpam: Boolean, address: String): List<Conversation>

    @Query("SELECT contactName FROM Conversation where address = :phoneNo")
    fun getContactName(phoneNo: String): String?

}