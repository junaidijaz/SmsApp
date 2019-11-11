package com.junaid.smsapp.adapters

/**
 * Created by R Ankit on 25-12-2016.
 */
interface ItemCLickListener {
    fun itemClicked(color: Int, contact: String, contactName: String?, id: String, threadId: String)
    //void itemLongClicked(int position,String contact,long id);
}
