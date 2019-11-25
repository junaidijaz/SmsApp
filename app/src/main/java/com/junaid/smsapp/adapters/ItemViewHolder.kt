package com.junaid.smsapp.adapters



import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.RecyclerView
import com.junaid.smsapp.R


class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var address: TextView = itemView.findViewById(R.id.title)
    var snippet: TextView = itemView.findViewById(R.id.snippet)
    var unread: ImageView = itemView.findViewById(R.id.unread)
    var avatars: ImageView = itemView.findViewById(R.id.avatars)
    var btnConversation : ConstraintLayout = itemView.findViewById(R.id.btnConversation)



}