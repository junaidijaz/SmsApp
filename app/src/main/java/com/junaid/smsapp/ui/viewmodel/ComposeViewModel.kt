package com.junaid.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.respository.ComposeRepository
import kotlinx.coroutines.launch

class ComposeViewModel(application: Application) : AndroidViewModel(application) {
    private val conversationRepository: ComposeRepository
    private val _application = application

    init {
        // the correct WordRepository.
        val conversationDao = ConversationRoomDatabase.getDatabase(application).conversationDao()
        conversationRepository = ComposeRepository(conversationDao)

    }

     fun changeConversationReadState(readState: String, threadId: String) = viewModelScope.launch {
        conversationRepository.changeConversationReadState(_application.applicationContext,readState, threadId)
    }
}