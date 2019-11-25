package com.junaid.smsapp.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ConversationAdapter
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import kotlinx.android.synthetic.main.fragment_spaming.view.*


class SpamFragment : Fragment(), ItemCLickListener {


    lateinit var mView: View
    lateinit var viewModel: ConversationViewModel
    var conversations = ArrayList<Conversation>()
    lateinit var adapter: ConversationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_spaming, container, false)

        buildRecyclerView()


        viewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        viewModel.spamConversations.observe(viewLifecycleOwner, Observer {
            it?.let {
                conversations.clear()
                conversations.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

        return mView
    }

    private fun buildRecyclerView() {
        adapter = ConversationAdapter(context!!, conversations)
        mView.rvSpaming.adapter = adapter
        adapter.setItemClickListener(this)
    }


    override fun itemClicked(
        color: Int,
        contact: String,
        contactName: String?,
        id: String,
        threadId: String
    ) {

    }

    override fun longItemClicked(
        color: Int,
        contact: String,
        contactName: String?,
        id: String,
        threadId: String,
        position: Int
    ) {
        showDialog("",
            "Are you sure you want to remove this number from spam?",
            "Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->
                viewModel.spamAddress(false,contact)
            },
            "No",
            DialogInterface.OnClickListener { dialogInterface, i -> },false)
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

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title).setCancelable(isCancelable).setMessage(msg)
        builder.setPositiveButton(positiveLabel, positiveClickButton)
        builder.setNegativeButton(negativeLabel, negativeClickButton)
        val alert = builder.create()
        alert.show()
        return alert
    }

}
