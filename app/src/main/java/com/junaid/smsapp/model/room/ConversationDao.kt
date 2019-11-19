package com.junaid.smsapp.model.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.junaid.smsapp.model.Conversation

@Dao
interface ConversationDao {


    @Query("SELECT * from Conversation ORDER BY time DESC")
    fun getAllConversation(): LiveData<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllConversation(conversation: List<Conversation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("DELETE FROM Conversation")
    suspend fun deleteAll()

    @Query("DELETE FROM Conversation where threadId = :threadId")
    suspend fun deleteConversation(threadId : String)

    @Query("SELECT threadId FROM Conversation where address = :address")
    suspend fun getThreadId(address : String)

}