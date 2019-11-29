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
    suspend fun insertAllConversation(conversation: LinkedHashSet<Conversation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(conversation: Conversation)

    @Query("update conversation set contactName = :name where address = :address")
    fun updateName(name: String, address: String)

    @Query("SELECT contactName FROM Conversation where address = :phoneNo")
    fun getContactName(phoneNo: String): String?

    @Query("DELETE FROM Conversation")
    suspend fun deleteAll()

    @Query("DELETE FROM Conversation where threadId = :threadId")
    suspend fun deleteConversation(threadId: String)

    @Query("SELECT threadId FROM Conversation where address = :address")
    fun getThreadId(address: String): String

    @Query("Update conversation set isBlocked = :flag where address = :address")
    suspend fun blockAddress(flag: Boolean, address: String)

    @Query("SELECT Count(address) FROM Conversation where isBlocked = :isBlocked and address = :address")
    fun getBlockedAddressCount(isBlocked: Boolean, address: String): Int

    @Query("SELECT * FROM Conversation where isBlocked = :isBlocked")
    fun getAllBlockedConversations(isBlocked: Boolean): LiveData<List<Conversation>>

    @Query("Update conversation set isSpam = :isSpam where address = :address")
    suspend fun setSpamAddress(isSpam: Boolean, address: String)

    @Query("SELECT * FROM Conversation where isSpam = 1")
    fun getAllSpamConversations(): LiveData<List<Conversation>>

    @Query("SELECT COUNT(address) FROM Conversation where isSpam = 1 and address = :address")
    fun getSpamConversationsCount(address: String): Int

    @Query("Select * from conversation where readState = 0 and isPinned = 0 and isBlocked = 0 and isSpam = 0 order by time DESC")
    fun getUnreadSms(): LiveData<List<Conversation>>

    @Query("Select * from conversation where readState = 1 and isPinned = 0 and isBlocked = 0 and isSpam = 0 order by time DESC")
    fun getReadSms(): LiveData<List<Conversation>>

    @Query("Select * from conversation where isPinned = 1 and isBlocked = 0  and isSpam = 0 order by time DESC")
    fun getPinnedSms(): LiveData<List<Conversation>>

    @Query("Update conversation set isPinned = :value where address = :address")
    fun markSmsPinned(value: Boolean, address: String)

    @Query("Update conversation set readState = :readState  where threadId = :threadId")
    suspend fun changeConversationReadState(readState: String, threadId: String)
}