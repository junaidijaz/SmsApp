package com.junaid.smsapp.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.R
import com.junaid.smsapp.utils.ColorGeneratorModified
 class ConversationAdapter(
    private var context: Context,
    var data: ArrayList<Conversation>
) :
    RecyclerView.Adapter<ConversationAdapter.ExampleViewHolder>() {
    private val generator = ColorGeneratorModified.MATERIAL
    private var itemClickListener: ItemCLickListener? = null

    fun setItemClickListener(itemClickListener: ItemCLickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation_list_item, parent, false)
        return ExampleViewHolder(v, itemClickListener)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = data[position]


        currentItem.contactName = getContactName(currentItem.address!!, context)


        holder.title.text = currentItem.contactName ?: currentItem.address
        holder.snippet.text = currentItem.msg
        val color = generator.getColor(currentItem.address)
        val firstChar = currentItem.address?.first()
        val drawable = TextDrawable.builder().buildRound(firstChar.toString(), color)
        holder.avatars.setImageDrawable(drawable)

        if (currentItem.readState.equals("0")) {
            holder.title.setTypeface(holder.title.typeface, Typeface.BOLD)
            holder.snippet.setTypeface(holder.snippet.typeface, Typeface.BOLD)
            holder.snippet.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.unread.visibility = View.VISIBLE
//            holder.time.setTypeface(holder.time.getTypeface(), Typeface.BOLD)
//            holder.time.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL)
            holder.snippet.setTypeface(null, Typeface.NORMAL)
            holder.unread.visibility = View.GONE
//            holder.time.setTypeface(null, Typeface.NORMAL)

        }


    }

    override fun getItemCount(): Int {
        return data.size
    }


   inner class ExampleViewHolder(
        itemView: View,
        itemClickListener: ItemCLickListener?

    ) :
        RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById(R.id.title)
        val snippet: TextView = itemView.findViewById(R.id.snippet)
        val avatars: ImageView = itemView.findViewById(R.id.avatars)
        val unread: ImageView = itemView.findViewById(R.id.unread)
        private val btnConversation: ConstraintLayout = itemView.findViewById(R.id.btnConversation)

        init {
            btnConversation.setOnClickListener {
                if (itemClickListener != null) {
                    data[adapterPosition].readState = "1"
                    notifyItemChanged(adapterPosition)

                    itemClickListener.itemClicked(
                        data[adapterPosition].color,
                        data[adapterPosition].address!!,
                        data[adapterPosition].contactName,
                        data[adapterPosition].id!!,
                        data[adapterPosition].threadId!!
                    )
                }
            }
        }

    }

    private fun getContactName(phoneNumber: String, context: Context): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var contactName = null as String?
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }

        return contactName
    }

}
