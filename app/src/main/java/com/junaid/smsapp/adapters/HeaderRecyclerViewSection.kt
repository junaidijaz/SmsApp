package com.junaid.smsapp.adapters


import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.junaid.smsapp.R
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.utils.ColorGeneratorModified
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection


class HeaderRecyclerViewSection(
    private val title: String,
    private val list: List<Conversation>
) :
    StatelessSection(R.layout.section_header_layout, R.layout.conversation_list_item) {

    private val generator = ColorGeneratorModified.MATERIAL
    private var itemClickListener: ItemCLickListener? = null

    fun setOnClickListener(itemClickListener : ItemCLickListener)
    {
        this.itemClickListener = itemClickListener
    }


    override fun getContentItemsTotal(): Int {
        return list.size
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    override fun onBindItemViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val iHolder: ItemViewHolder = holder as ItemViewHolder
        val currentItem = list[position]
        currentItem.apply {

            iHolder.address.text = contactName ?: address
            iHolder.snippet.text = msg
                val color = generator.getColor(currentItem.address)
                val firstChar =address.first()
                val drawable = TextDrawable.builder().buildRound(firstChar.toString(), color)
            iHolder.avatars.setImageDrawable(drawable)

                if (readState.equals("0")) {
                    iHolder.address.setTypeface(holder.address.typeface, Typeface.BOLD)
                    iHolder.snippet.setTypeface(holder.snippet.typeface, Typeface.BOLD)
//                    iHolder.snippet.setTextColor(ContextCompat.getColor(context, R.color.black))
                    iHolder.unread.visibility = View.VISIBLE
                } else {
                    iHolder.address.setTypeface(null, Typeface.NORMAL)
                    iHolder.snippet.setTypeface(null, Typeface.NORMAL)
                    iHolder.unread.visibility = View.GONE
                }

            iHolder.btnConversation.setOnLongClickListener {

                if(itemClickListener != null)
                {
                    currentItem.readState = "1"

                    itemClickListener?.longItemClicked(

                        currentItem.color,
                        currentItem.address,
                        currentItem.contactName,
                        currentItem.id,
                        currentItem.threadId,
                        position
                    )
                }
                true
            }
        }

    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        val hHolder: HeaderViewHolder = holder as HeaderViewHolder
        hHolder.headerTitle.text = title
    }

    companion object {
        private val TAG = HeaderRecyclerViewSection::class.java.simpleName
    }

}