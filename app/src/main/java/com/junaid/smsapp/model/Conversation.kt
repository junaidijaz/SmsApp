package com.junaid.smsapp.model

import android.telephony.PhoneNumberUtils
import android.util.Log

data class Conversation(
    var id: String? = null,
    var address: String? = null,
    var contactName: String? = null,
    var msg: String? = null,
    var threadId: String? = null,
    var readState: String? = null, //"0" for have not read sms and "1" for have read sms
    var time: String? = null,
    var folderName: String? = null,
    var color: Int = 0


) {
    override fun equals(other: Any?): Boolean {
        val sms = other as Conversation
        return PhoneNumberUtils.compare(address,sms.address)
    }

    override fun hashCode(): Int {
        return this.address.hashCode()
    }
}