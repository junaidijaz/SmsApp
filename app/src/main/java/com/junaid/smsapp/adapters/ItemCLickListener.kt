package com.junaid.smsapp.adapters

import java.text.ParsePosition


interface ItemCLickListener {
    fun itemClicked(color: Int, contact: String, contactName: String?, id: String, threadId: String)
    fun longItemClicked(color: Int, contact: String, contactName: String?, id: String, threadId: String,position: Int)
    //void itemLongClicked(int position,String contact,long id);
}
