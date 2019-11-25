package com.junaid.smsapp.adapters


import android.view.View

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.junaid.smsapp.R


class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var headerTitle: TextView = itemView.findViewById(R.id.header_id)

}