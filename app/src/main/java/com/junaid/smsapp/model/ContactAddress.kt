package com.junaid.smsapp.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ContactAddress(var name : String? = null,@PrimaryKey var address : String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()!!
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContactAddress> {
        override fun createFromParcel(parcel: Parcel): ContactAddress {
            return ContactAddress(parcel)
        }

        override fun newArray(size: Int): Array<ContactAddress?> {
            return arrayOfNulls(size)
        }
    }
}