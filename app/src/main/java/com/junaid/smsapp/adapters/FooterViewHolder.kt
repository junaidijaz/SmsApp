package com.junaid.smsapp.adapters

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.junaid.smsapp.R

class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvSeeMore = itemView.findViewById(R.id.tvSeeMore) as TextView
    val rootView = itemView.findViewById(R.id.rootView) as RelativeLayout
}