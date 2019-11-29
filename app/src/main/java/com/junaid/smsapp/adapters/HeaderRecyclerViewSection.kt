package com.junaid.smsapp.adapters


import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.junaid.smsapp.R
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.fragments.SmartInboxFragment
import com.junaid.smsapp.utils.ColorGeneratorModified
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters


class HeaderRecyclerViewSection(
   private val icDrawable: Drawable?,
    private val title: String,
    private val list: List<Conversation>
) :
    Section(
        SectionParameters.builder().headerResourceId(R.layout.section_header_layout)
            .footerResourceId(R.layout.section_footer)
            .itemResourceId(R.layout.conversation_list_item).build()
    ) {
    interface OnFooterClicked {
        fun onFooterClicked()
    }

   private var onFooterClickedListener: OnFooterClicked? = null


    private val generator = ColorGeneratorModified.MATERIAL
    private var itemClickListener: ItemCLickListener? = null

    fun setOnClickListener(itemClickListener: ItemCLickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setOnFooterClickListener(onFooterClicked: OnFooterClicked)
    {
        this.onFooterClickedListener = onFooterClicked
    }

    override fun getContentItemsTotal(): Int {
        return if (list.size > 3) {
            3
        } else list.size
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
            val firstChar = address.first()
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

            iHolder.btnConversation.setOnClickListener {
                itemClickListener?.itemClicked(
                    currentItem.color,
                    currentItem.address,
                    currentItem.contactName,
                    currentItem.id,
                    currentItem.threadId
                )

            }

            iHolder.btnConversation.setOnLongClickListener {
                if (itemClickListener != null) {
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

    override fun getFooterViewHolder(view: View): RecyclerView.ViewHolder? {
        return FooterViewHolder(view)
    }


    override fun onBindFooterViewHolder(holder: RecyclerView.ViewHolder?) {
        super.onBindFooterViewHolder(holder)
        val footerHolder: FooterViewHolder = holder as FooterViewHolder

        if (list.size < 4) {
            footerHolder.rootView.visibility = View.GONE
        } else {
            footerHolder.rootView.visibility = View.VISIBLE
            footerHolder.tvSeeMore.text = "see all(${list.size})"
            footerHolder.rootView.setOnClickListener {
                this.onFooterClickedListener?.onFooterClicked()
            }
//        }
        }

    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        val hHolder: HeaderViewHolder = holder as HeaderViewHolder
        holder.headerTitle.setCompoundDrawablesWithIntrinsicBounds(icDrawable,null,null,null)
        hHolder.headerTitle.text = title
    }

    companion object {
        private val TAG = HeaderRecyclerViewSection::class.java.simpleName
    }

}