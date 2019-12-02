package com.junaid.smsapp.respository

import android.content.Context
import android.os.AsyncTask
import com.junaid.smsapp.model.room.ContactsDao
import com.junaid.smsapp.utils.SmsContract

class ContactsRepository(private val contactsDao: ContactsDao) {

    fun addAllContacts(context: Context ) {
        InsertContacts(contactsDao, context).execute()
    }

     fun getContacts() = contactsDao.getAllContacts()


    class InsertContacts internal constructor(private val dao: ContactsDao, var context: Context) :
        AsyncTask<Void, Int, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            dao.insertAllContacts(SmsContract.getContactList(context))
            return null
        }
    }

}