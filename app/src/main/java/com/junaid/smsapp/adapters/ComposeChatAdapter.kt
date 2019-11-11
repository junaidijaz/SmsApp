package com.junaid.smsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.junaid.smsapp.R
import com.junaid.smsapp.model.Conversation
import java.text.SimpleDateFormat
import java.util.*


class ComposeChatAdapter(private var data: List<Conversation>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view: View

        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_item_out, parent, false)
            SentMessageHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_item_in, parent, false)
            ReceivedMessageHolder(view)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentItem = data[position]

        if (holder.itemViewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            val h = holder as ReceivedMessageHolder
            h.bind(currentItem)
        } else {
            val h = holder as SentMessageHolder
            h.bind(currentItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = data[position]

        return if (currentItem.folderName == "inbox") {
            VIEW_TYPE_MESSAGE_RECEIVED
        } else {
            VIEW_TYPE_MESSAGE_SENT
        }

    }


    private inner class ReceivedMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView = itemView.findViewById(R.id.body)
        internal var timeText: TextView = itemView.findViewById(R.id.timestamp)



        internal fun bind(message: Conversation) {
            messageText.text = message.msg
            timeText.text = (timeStampToDate(message.time))
        }
    }


    private inner class SentMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView = itemView.findViewById(R.id.body)
        internal var timeText: TextView = itemView.findViewById(R.id.timestamp)

        internal fun bind(message: Conversation) {
            messageText.text = message.msg
            timeText.text = (timeStampToDate(message.time))
        }
    }

    fun timeStampToDate(timeStamp: String?): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(timeStamp!!.toLong()))

    }

}