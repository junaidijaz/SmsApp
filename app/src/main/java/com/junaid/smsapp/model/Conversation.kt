package com.junaid.smsapp.model

import android.telephony.PhoneNumberUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull


@Entity
data class Conversation(
    @PrimaryKey @NotNull var id: String = "",
    var address: String = "",
    var contactName: String? = null,
    var msg: String? = null,
    @NotNull var threadId: String = "",
    var readState: String? = null, //"0" for have not read sms and "1" for have read sms
    var time: String? = null,
    var isBlocked: Boolean = false,
    var isSpam: Boolean = false,
    var folderName: String? = null,
    var color: Int = 0,
    var isPinned : Boolean = false

) {
    init {
        time = System.currentTimeMillis().toString()
    }

    override fun equals(other: Any?): Boolean {
        val sms = other as Conversation
        return PhoneNumberUtils.compare(address, sms.address)
    }

    override fun hashCode(): Int {
        return this.address.hashCode()
    }
}