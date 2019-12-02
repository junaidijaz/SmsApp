package com.junaid.smsapp.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.junaid.smsapp.model.ContactAddress

@Dao
interface ContactsDao {

    @Query("SELECT * from ContactAddress   ORDER BY name ASC")
    fun getAllContacts(): List<ContactAddress>

    @Insert(onConflict = OnConflictStrategy.IGNORE )
    fun insertAllContacts(listContacts: List<ContactAddress>)

}