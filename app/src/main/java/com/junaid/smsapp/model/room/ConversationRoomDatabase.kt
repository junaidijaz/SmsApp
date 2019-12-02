package com.junaid.smsapp.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.junaid.smsapp.model.ContactAddress
import com.junaid.smsapp.model.Conversation


// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [Conversation::class , ContactAddress::class], version = 2, exportSchema = false)
abstract class ConversationRoomDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun contactsDao(): ContactsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ConversationRoomDatabase? = null

        fun getDatabase(context: Context): ConversationRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConversationRoomDatabase::class.java,
                    "word_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }

    }
}