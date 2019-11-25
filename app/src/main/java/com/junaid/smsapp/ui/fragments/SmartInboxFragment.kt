package com.junaid.smsapp.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.HeaderRecyclerViewSection
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_smart_inbox.view.*


class SmartInboxFragment : Fragment() {

    lateinit var mView: View
    var readSmsList = ArrayList<Conversation>()
    var unreadSmsList = ArrayList<Conversation>()
    var pinnedSms = ArrayList<Conversation>()
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var sectionAdapter: SectionedRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_smart_inbox, container, false)

        buildRecyclerView()

        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        conversationViewModel.readSms.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d("TAG", "inViewModel: ")
                readSmsList.clear()
                readSmsList.addAll(LinkedHashSet<Conversation>(it))
                sectionAdapter.notifyDataSetChanged()

            }
        })

        conversationViewModel.unreadSms.observe(viewLifecycleOwner, Observer {
            it?.let {
                unreadSmsList.clear()
                unreadSmsList.addAll(java.util.LinkedHashSet<Conversation>(it))
                sectionAdapter.notifyDataSetChanged()
            }
        })

        conversationViewModel.pinnedSms.observe(viewLifecycleOwner, Observer {
            it?.let {
                pinnedSms.clear()
                pinnedSms.addAll(java.util.LinkedHashSet<Conversation>(it))
                sectionAdapter.notifyDataSetChanged()
            }
        })

        return mView
    }

    private fun buildRecyclerView() {
        mView.rvSmartInbox.layoutManager = LinearLayoutManager(context)
        mView.rvSmartInbox.setHasFixedSize(true)


        val firstSection =
            HeaderRecyclerViewSection("Notifications", unreadSmsList)
        val secondSection =
            HeaderRecyclerViewSection("Pinned", pinnedSms)
        val thirdSection =
            HeaderRecyclerViewSection("Seen", readSmsList)
        sectionAdapter = SectionedRecyclerViewAdapter()
        sectionAdapter.addSection(firstSection)
        sectionAdapter.addSection(secondSection)
        sectionAdapter.addSection(thirdSection)
        mView.rvSmartInbox.adapter = sectionAdapter

        
        firstSection.setOnClickListener(object : ItemCLickListener {
            override fun longItemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String,
                position: Int
            ) {
                unreadSmsList[position].let {
                    showConversationDialog(
                        it.contactName,
                        it.address,
                        position,
                        it.threadId,
                        it.isPinned
                    )
                }

            }

            override fun itemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String
            ) {

            }
        })

        secondSection.setOnClickListener(object : ItemCLickListener {
            override fun longItemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String,
                position: Int
            ) {

                pinnedSms[position].let {
                    showConversationDialog(
                        it.contactName,
                        it.address,
                        position,
                        it.threadId,
                        it.isPinned
                    )
                }
            }

            override fun itemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String
            ) {

            }
        })
        thirdSection.setOnClickListener(object : ItemCLickListener {
            override fun longItemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String,
                position: Int
            ) {

                readSmsList[position].let {
                    showConversationDialog(
                        it.contactName,
                        it.address,
                        position,
                        it.threadId,
                        it.isPinned
                    )
                }
            }

            override fun itemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String
            ) {

            }
        })


    }

    private fun showConversationDialog(
        contactName: String?,
        address: String,
        position: Int,
        threadId: String,
        pinned: Boolean
    ) {

        val id = if (pinned) {
            R.array.convo_options_with_unpin
        } else {
            R.array.convo_options_with_pin
        }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(contactName ?: address)
            .setItems(id,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> {
                            conversationViewModel.deleteConversation(threadId)
                        }
                        1 -> {
                            conversationViewModel.spamAddress(true, address)
                        }
                        2 -> {
                            conversationViewModel.blockAddress(true, address)
                        }
                        3 -> {
                            conversationViewModel.pinSms(!pinned, address)
                        }
                    }

                }).show()
    }

}
