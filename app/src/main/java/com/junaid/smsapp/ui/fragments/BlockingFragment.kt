package com.junaid.smsapp.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.junaid.smsapp.R
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import com.junaid.smsapp.utils.SmsContract
import kotlinx.android.synthetic.main.fragment_blocking.view.*


class BlockingFragment : Fragment() {

    lateinit var mView: View
    lateinit var viewModel: ConversationViewModel
    lateinit var adapter: ArrayAdapter<String>
    var addresses = ArrayList<String>()
    var conversations = ArrayList<Conversation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_blocking, container, false)

        adapter = ArrayAdapter(context!!, R.layout.blocking_list_item, R.id.title, addresses)

        mView.lvBlocking.adapter = adapter
        mView.lvBlocking.setOnItemClickListener { adapterView, view, position, l ->

            showDialog(
                "",
                "are you sure you want to unblock this contact?", "Yes",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    viewModel.blockAddress(false, conversations[position].address!!)
                },
                "No",
                DialogInterface.OnClickListener { dialogInterface, i -> },
                false
            )


        }

        viewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        viewModel.getBlockedNumbers.observe(viewLifecycleOwner, Observer {
            it?.let {
                addresses.clear()
                conversations.clear()
                conversations.addAll(LinkedHashSet<Conversation>(it))
                for (k in conversations) {
                    addresses.add(viewModel.getContactName(k.address) ?: k.address)
                }
                adapter.notifyDataSetChanged()
            }
        })

        return mView
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
