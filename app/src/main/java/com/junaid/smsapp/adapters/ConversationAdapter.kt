package com.junaid.smsapp.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.junaid.smsapp.R
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.utils.ColorGeneratorModified
import com.junaid.smsapp.utils.SmsContract


class ConversationAdapter(
    var context: Context,
    var data: ArrayList<Conversation>
) :
    RecyclerView.Adapter<ConversationAdapter.ExampleViewHolder>() {
    private val generator = ColorGeneratorModified.MATERIAL
    private var itemClickListener: ItemCLickListener? = null
    private var itemSwipeLisetner: OnSwipeLisetener? = null


    fun setItemClickListener(itemClickListener: ItemCLickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setItemSwipeListener(itemClickListener: OnSwipeLisetener) {
        this.itemSwipeLisetner = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.conversation_list_item, parent, false)
        return ExampleViewHolder(v, itemClickListener)
    }

    fun deleteItem(position: Int) {
        itemSwipeLisetner?.onSwipeLeft(position)
    }

    fun archiveItem(position: Int) {
        itemSwipeLisetner?.onSwipeRight(position)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = data[position] as Conversation?


        currentItem?.apply {

            contactName = SmsContract.getContactName(address, context)


            holder.title.text = contactName ?: address
            holder.snippet.text = msg
            val color = generator.getColor(currentItem.address)
            val firstChar =address?.first()
            val drawable = TextDrawable.builder().buildRound(firstChar.toString(), color)
            holder.avatars.setImageDrawable(drawable)

            if (readState.equals("0")) {
                holder.title.setTypeface(holder.title.typeface, Typeface.BOLD)
                holder.snippet.setTypeface(holder.snippet.typeface, Typeface.BOLD)
                holder.snippet.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.unread.visibility = View.VISIBLE
            } else {
                holder.title.setTypeface(null, Typeface.NORMAL)
                holder.snippet.setTypeface(null, Typeface.NORMAL)
                holder.unread.visibility = View.GONE
            }

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

            btnConversation.setOnLongClickListener {
                if (itemClickListener != null) {
                    data[adapterPosition].readState = "1"


                    itemClickListener.longItemClicked(

                        data[adapterPosition].color,
                        data[adapterPosition].address!!,
                        data[adapterPosition].contactName,
                        data[adapterPosition].id,
                        data[adapterPosition].threadId,
                        adapterPosition
                    )
                }
                true
            }

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


}
