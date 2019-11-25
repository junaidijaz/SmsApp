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
    val spamConversations: LiveData<List<Conversation>>
    val pinnedSms : LiveData<List<Conversation>>
    val readSms : LiveData<List<Conversation>>
    val unreadSms : LiveData<List<Conversation>>
    var _application : Application = application

    init {
        // the correct WordRepository.
        val conversationDao = ConversationRoomDatabase.getDatabase(application).conversationDao()
        conversationRepository = ConversationRepository(conversationDao)
        allConversation = conversationRepository.allConversations
        getBlockedNumbers = conversationRepository.getAllBlockedConversations
        spamConversations = conversationRepository.spamConversations
        pinnedSms = conversationRepository.pinnedSms
        readSms = conversationRepository.readSms
        unreadSms = conversationRepository.unReadSms
    }

    /**
     * The implementation of insertAllConversation() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */

    fun insertAllConversation(conversations: ArrayList<Conversation>) = viewModelScope.launch {
        conversationRepository.insertConversationList(conversations,_application)
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

    fun pinSms(flag : Boolean, address : String) = viewModelScope.launch {
        conversationRepository.pinSms(flag , address)
    }

    fun getSpamConversations() = viewModelScope.launch {
        conversationRepository.getSpamConversation()
    }

    suspend fun deleteAllConversation() = viewModelScope.launch {
        conversationRepository.deleteAllConversation()
    }

    fun getThreadId(phoneNo: String) = viewModelScope.launch {
        conversationRepository.getThreadId(phoneNo)
    }
}