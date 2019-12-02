package com.junaid.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.junaid.smsapp.model.ContactAddress
import com.junaid.smsapp.model.room.ConversationRoomDatabase
import com.junaid.smsapp.respository.ContactsRepository
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsRepository: ContactsRepository
    var mApplication = application

    init {
        val conversationDao = ConversationRoomDatabase.getDatabase(application).contactsDao()
        contactsRepository = ContactsRepository(conversationDao)
    }

    fun getAllContacts() = contactsRepository.getContacts()


    fun  insertContacts() = viewModelScope.launch {
        contactsRepository.addAllContacts(mApplication.applicationContext)
    }

}