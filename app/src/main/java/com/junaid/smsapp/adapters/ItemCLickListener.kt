package com.junaid.smsapp.adapters


interface ItemCLickListener {
    fun itemClicked(color: Int, contact: String, contactName: String?, id: String, threadId: String)
    //void itemLongClicked(int position,String contact,long id);
}
