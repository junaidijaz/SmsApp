package com.junaid.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.respository.ConversationRepository
import kotlinx.coroutines.launch

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val conversationRepository: ConversationRepository
    val allConversation: LiveData<List<Conversation>>
    val getBlockedNumbers: LiveData<List<Conversation>>

    init {

        // the correct WordRepository.
        val conversationDao = ConversationRoomDatabase.getDatabase(application).conversationDao()
        conversationRepository = ConversationRepository(conversationDao)
        allConversation = conversationRepository.allConversations
        getBlockedNumbers = conversationRepository.getAllBlockedConversations
    }

    /**
     * The implementation of insertAllConversation() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insertAllConversation(conversations: ArrayList<Conversation>) = viewModelScope.launch {
        conversationRepository.insertConversationList(conversations)
    }

    fun insertConversation(conversation: Conversation) = viewModelScope.launch {
        conversationRepository.insertConversation(conversation)
    }

    fun deleteConversation(threadId: String) = viewModelScope.launch {
        conversationRepository.deleteConversation(threadId)
    }


    fun blockAddress(flag: Boolean, phoneNo: String) = viewModelScope.launch {
        conversationRepository.blockAddress(flag, phoneNo)
    }

    fun spamAddress(flag: Boolean, phoneNo: String) = viewModelScope.launch {
        conversationRepository.spamAddress(flag, phoneNo)
    }

    fun deleteAllConversation() = viewModelScope.launch {
        conversationRepository.deleteAllConversation()
    }

    fun getThreadId(phoneNo: String) = viewModelScope.launch {
        conversationRepository.getThreadId(phoneNo)
    }
}