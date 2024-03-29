package com.junaid.smsapp.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ConversationAdapter
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.ui.ComposeActivity
import com.junaid.smsapp.ui.viewmodel.ContactsViewModel
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import com.junaid.smsapp.utils.MyPreference
import com.junaid.smsapp.utils.SmsContract
import kotlinx.android.synthetic.main.fragment_inbox.view.*

class InboxFragment : Fragment() {

    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var mRecentlyDeletedItem: Conversation
    private var mRecentlyDeletedItemPosition = -1
    private var recipients = ArrayList<String>()
    private var isSearching = false


    private val appPermissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )

    var isDefault: Boolean = false //is this app is default
    var convoList = ArrayList<Conversation>()
    var tempFullconvoList = ArrayList<Conversation>()

    lateinit var adapter: ConversationAdapter
    lateinit var mView: View

    private val PERMISSION_REQUEST_CODE = 1240
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_inbox, container, false)

        isSearching = false

        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        contactsViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        conversationViewModel.getAllConversation("")
            .observe(viewLifecycleOwner, Observer {
                it?.let {
                    if (!isSearching) {
                        convoList.clear()
                        tempFullconvoList.clear()
                        convoList.addAll(LinkedHashSet<Conversation>(it))
                        tempFullconvoList.addAll(LinkedHashSet<Conversation>(it))
                        if (::adapter.isInitialized)
                            adapter.notifyDataSetChanged()
                    }
                }
            })

        mView.fabNewSms.setOnClickListener {
            recipients.clear()
            for (item in convoList) {
                recipients.add(item.address)
            }
            val intent = Intent(requireContext(), ComposeActivity::class.java)
            intent.putStringArrayListExtra("recipients", recipients)
            intent.putExtra("fromFab", "new")
            startActivity(intent)
        }

        mView.fabSearchSms.setOnClickListener {
            isSearching = !isSearching
            mView.searchViewLayout.visibility = View.VISIBLE
            showNewSearchFab(false)
        }

        mView.btnHideSearch.setOnClickListener {

            showNewSearchFab(true)
            mView.searchViewLayout.visibility = View.GONE
            convoList.clear()
            convoList.addAll(tempFullconvoList)
            adapter.notifyDataSetChanged()
            isSearching = !isSearching
        }


        mView.etSearchSms.addTextChangedListener {
            Log.d("TAG", "onCreateView:  ${it.toString()}")
            conversationViewModel.getAllConversation(it.toString())
                .observe(viewLifecycleOwner, Observer {
                    if (isSearching) {
                        convoList.clear()
                        convoList.addAll(LinkedHashSet<Conversation>(it))
                        if (::adapter.isInitialized)
                            adapter.notifyDataSetChanged()
                    }
                })

        }

        /**check for app permissions
         * in case one or more permissions are not granted
         * activityCompact.requestPermissions will request permissions
         * and the control goes to  onRequestPermissionsResult() callback method
         **/
        checkAndRequestPermissions()
        setInboxTextChangesLis()
        return mView
    }

    private fun showNewSearchFab(flag: Boolean) {
        if (flag) {
            (mView.fabSearchSms as View).visibility = View.VISIBLE
            (mView.fabNewSms as View).visibility = View.VISIBLE
        } else {
            (mView.fabSearchSms as View).visibility = View.GONE
            (mView.fabNewSms as View).visibility = View.GONE

        }
    }


    override fun onResume() {
        Log.d("TAG", "onResume: ")
        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)

        if (isDefault) {
            buildSmsRecyclerView()
            syncSms()
        }
        NotificationHelper.removeNotification(requireContext(), null)
        super.onResume()
    }

    /**
     *this function will detect text change in edit text and query required sms
     */
    private fun
            setInboxTextChangesLis() {
//        toolbarSearch.addTextChangedListener {
//            refreshConversationList(it.toString())
//        }
    }

    /**
     * fun will check permissions to be asked
     */
    private fun checkAndRequestPermissions(): Boolean {
        val listPermissionNeeded = ArrayList<String>()

        for (per in appPermissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    per
                ) != PackageManager.PERMISSION_GRANTED
            )
                listPermissionNeeded.add(per)
        }

        //ask for not granted permission
        if (listPermissionNeeded.isNotEmpty()) {
            requestPermissions(
                listPermissionNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )

            return false
        }

        askForDefaultSms()

        //app has all permissions
        return true
    }

    private fun showUndoSnackbar() {
        val view = activity?.findViewById<DrawerLayout>(R.id.drawerLayout)
        val snackbar = Snackbar.make(
            view!!, "Conversation archived...",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo") { undoDelete() }
        snackbar.show()
    }

    private fun undoDelete() {
        convoList.add(
            mRecentlyDeletedItemPosition,
            mRecentlyDeletedItem
        )
        adapter.notifyItemInserted(mRecentlyDeletedItemPosition)
    }


    private fun askForDefaultSms() {
        //all Permissions are given sync msgs
        val myPackageName = activity?.packageName
        if (Telephony.Sms.getDefaultSmsPackage(context) != myPackageName) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName)
            startActivityForResult(intent, 1)
            isDefault = false
            MyPreference.getInstance(requireContext())?.saveData("syncingFirstTime", "")
        } else {
            isDefault = true
            buildSmsRecyclerView()
            syncSms()
        }
    }

    private fun syncSms() {
        if (MyPreference.getInstance(requireContext())?.getData("syncingFirstTime") == "")
            conversationViewModel.insertAllConversation(getAllSms())

        syncContacts()
    }

    private fun syncContacts() {
        contactsViewModel.insertContacts()
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


    fun deleteItem(position: Int) {
        mRecentlyDeletedItem = convoList[position]
        mRecentlyDeletedItemPosition = position
        convoList.removeAt(position)
        adapter.notifyItemRemoved(position)
        Log.d("TAG", "deleteItem: ${convoList.size}")
        showUndoSnackbar()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        Log.d("TAG", "onRequestPermissionsResult: ")

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionResults = HashMap<String, Int>()
            var deniedCount = 0

            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults[permissions[i]] = grantResults[i]
                    deniedCount++
                }
            }

            //if deniedCount == 0 means all permissions are granted
            if (deniedCount != 0) {
                for (entry in permissionResults.entries) {
                    val permName = entry.key
                    val permResult = entry.value

                    /**
                     * permission is denied
                     * so ask again explaining the usage of permission
                     * shouldShowRequestPermissionRationale will return true
                     */
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permName)) {

                        showDialog(
                            "",
                            "This app needs SMS permissions to work properly",
                            "Yes, Grant permissions",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()

                                checkAndRequestPermissions()
                            },
                            "No, Exit app",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                                activity?.finish()
                            }, false
                        )
                    } else {
                        showDialog(
                            "",
                            "You have denied some permissions, Allow All permissions at [Setting] > [Permissions]",
                            "Go to settings",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", activity?.packageName, null)
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                activity?.finish()
                            },
                            "No, Exit app",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                                activity?.finish()
                            }, false
                        )
                    }
                }
            } else {
                askForDefaultSms()
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            askForDefaultSms()
        }
    }


    private fun getAllSms(): ArrayList<Conversation> {

        val lstSms = ArrayList<Conversation>()

        val cr = activity?.contentResolver
        val c =
            cr?.query(
                SmsContract.ALL_SMS_URI,
                null,
                null,
                null,
                SmsContract.SORT_DESC
            )
        val totalSMS = c!!.count

        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val objSms = Conversation()
                objSms.id = c.getString(c.getColumnIndexOrThrow("_id"))
                //number of conversation
                objSms.address = c.getString(c.getColumnIndexOrThrow(SmsContract.ADDRESS)) ?: ""
                objSms.msg = c.getString(c.getColumnIndexOrThrow(SmsContract.BODY))
                objSms.threadId = c.getString(c.getColumnIndexOrThrow(SmsContract.THREADID))
                //1 if msg is read
                objSms.readState = c.getString(c.getColumnIndex(SmsContract.READ))
                objSms.time = c.getString(c.getColumnIndexOrThrow(SmsContract.DATE))

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.folderName = "inbox"
                } else {
                    objSms.folderName = "sent"
                }



                if (objSms.address != "") {
                    lstSms.add(objSms)
                }


                c.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }

        c.close()

        for (i in lstSms.indices) {
            if (lstSms[i].address != "") {
                if (lstSms[i].address.contains("+92")) {
                    lstSms[i].address = lstSms[i].address.replace("+92", "0")
                }
            }
        }
        return ArrayList(LinkedHashSet<Conversation>(lstSms))
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


}
