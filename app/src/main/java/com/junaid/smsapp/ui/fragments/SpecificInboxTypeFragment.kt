package com.junaid.smsapp.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ConversationAdapter
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.ComposeActivity
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import com.junaid.smsapp.utils.SmartInboxOptions
import com.junaid.smsapp.utils.SmsContract
import kotlinx.android.synthetic.main.fragment_inbox.view.*


class SpecificInboxTypeFragment : Fragment() {

    private lateinit var conversationViewModel: ConversationViewModel

    var convoList = ArrayList<Conversation>()
    lateinit var toObserve: LiveData<List<Conversation>>

    lateinit var adapter: ConversationAdapter
    lateinit var mView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_inbox, container, false)
        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        val selectedOption = arguments?.getSerializable("data") as SmartInboxOptions

        toObserve = when (selectedOption) {
            SmartInboxOptions.Notifications -> conversationViewModel.unreadSms
            SmartInboxOptions.Seen -> conversationViewModel.readSms
            else -> conversationViewModel.pinnedSms
        }


        toObserve.observe(viewLifecycleOwner, Observer {
            it?.let {
                convoList.clear()
                convoList.addAll(LinkedHashSet<Conversation>(it))
                if (::adapter.isInitialized)
                    adapter.notifyDataSetChanged()
            }
        })

        (mView.fabNewSms as View).visibility = View.GONE
        (mView.fabSearchSms as View).visibility = View.GONE

        buildSmsRecyclerView()

        return mView
    }


    private fun buildSmsRecyclerView() {

        mView.recyclerView.setHasFixedSize(true)
        val lm = LinearLayoutManager(context)
        lm.isSmoothScrollbarEnabled = true
        mView.recyclerView.layoutManager = lm
        adapter = ConversationAdapter(requireContext(), convoList)
//        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
//        itemTouchHelper.attachToRecyclerView(recyclerView)
        mView.recyclerView.adapter = adapter

//        adapter.setItemSwipeListener(object : OnSwipeLisetener {
//            override fun onSwipeLeft(position: Int) {
//                deleteItem(position)
//            }
//
//            override fun onSwipeRight(position: Int) {
//                deleteItem(position)
//
//            }
//        })


        adapter.setItemClickListener(object : ItemCLickListener {
            override fun itemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String
            ) {
                val intent = Intent(context, ComposeActivity::class.java)
                intent.putExtra(SmsContract.ADDRESS, contact)
                intent.putExtra(SmsContract.CONTACTNAME, contactName)
                intent.putExtra(SmsContract.THREADID, threadId)
                startActivity(intent)

            }

            override fun longItemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String,
                position: Int
            ) {

                showConversationDialog(contactName, contact, position, threadId)

            }
        })
    }

    private fun showConversationDialog(
        contactName: String?,
        address: String,
        position: Int,
        threadId: String
    ) {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(contactName ?: address)
            .setItems(R.array.convo_options,
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
                    }

                })
        builder.create().show()
    }


    private fun showDialog(
        title: String,
        msg: String,
        positiveLabel: String,
        positiveClickButton: DialogInterface.OnClickListener,
        negativeLabel: String,
        negativeClickButton: DialogInterface.OnClickListener,
        isCancelable: Boolean
    ): AlertDialog {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title).setCancelable(isCancelable).setMessage(msg)
        builder.setPositiveButton(positiveLabel, positiveClickButton)
        builder.setNegativeButton(negativeLabel, negativeClickButton)
        val alert = builder.create()
        alert.show()
        return alert
    }


    companion object {
        fun getInstance(selectedOption: SmartInboxOptions): SpecificInboxTypeFragment {
            val frag = SpecificInboxTypeFragment()
            val args = Bundle()
            args.putSerializable("data", selectedOption)
            frag.arguments = args
            return frag

        }
    }

}
