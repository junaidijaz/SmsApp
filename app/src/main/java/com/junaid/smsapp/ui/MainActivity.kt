package com.junaid.smsapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.junaid.smsapp.NotificationHelper
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ConversationAdapter
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.adapters.OnSwipeLisetener
import com.junaid.smsapp.adapters.SwipeToDeleteCallback
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.revicers.OnSmsReceived
import com.junaid.smsapp.revicers.SmsReceiver
import com.junaid.smsapp.ui.viewmodel.ConversationViewModel
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import com.junaid.smsapp.utils.SmsContract.Companion.ALL_SMS_URI
import com.junaid.smsapp.utils.SmsContract.Companion.CONTACTNAME
import com.junaid.smsapp.utils.SmsContract.Companion.DATE
import com.junaid.smsapp.utils.SmsContract.Companion.READ
import com.junaid.smsapp.utils.SmsContract.Companion.THREADID
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnSmsReceived {

    private lateinit var mRecentlyDeletedItem: Conversation
    private var mRecentlyDeletedItemPosition = -1

    private lateinit var conversationViewModel: ConversationViewModel

    private val appPermissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )
    var isDefault: Boolean = false //is this app is default
    var convoList = ArrayList<Conversation>()
    lateinit var adapter: ConversationAdapter


    private val PERMISSION_REQUEST_CODE = 1240

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TAG", "onCreate: ")

        conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)
        conversationViewModel.allConversation.observe(this, Observer {
            it?.let {
                Log.d("TAG", "inViewModel: ")
                convoList.clear()
                convoList.addAll(it)
                if (::adapter.isInitialized)
                    adapter.notifyDataSetChanged()
            }
        })

        /**check for app permissions
         * in case one or more permissions are not granted
         * activityCompact.requestPermissions will request permissions
         * and the control goes to onRequestPermissionsResult() callback method
         **/
        checkAndRequestPermissions()
        setInboxTextChangesLis()


    }


    /**
     *this function will change
     */
    private fun setInboxTextChangesLis() {
        toolbarSearch.addTextChangedListener {
            refreshConversationList(it.toString())
        }
    }

    override fun onResume() {

        Log.d("TAG", "onResume: ")
        if (!::conversationViewModel.isInitialized)
            conversationViewModel = ViewModelProvider(this).get(ConversationViewModel::class.java)

        if (isDefault) {
            buildSmsRecyclerView()
            refreshConversationList(null)
        }
        NotificationHelper.removeNotification(this, null)
        SmsReceiver.setOnSmsLisenter(this)
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        SmsReceiver.removeSmsLisenter()
    }


    /**
     * fun will check permissions to be asked
     */
    private fun checkAndRequestPermissions(): Boolean {
        val listPermissionNeeded = ArrayList<String>()

        for (per in appPermissions) {
            if (ContextCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(per)
        }

        //ask for not granted permission
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, listPermissionNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )

            return false
        }

        //all Permissions are given sync msgs
        val myPackageName = packageName
        if (Telephony.Sms.getDefaultSmsPackage(this) != myPackageName) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName)
            startActivityForResult(intent, 1)
            isDefault = false
        } else {
            isDefault = true
            buildSmsRecyclerView()
            refreshConversationList(null)
        }


        //app has all permissions
        return true
    }

    override fun onSmsReceived() {
        refreshConversationList(null)
    }

    private fun refreshConversationList(query: String?) {
        conversationViewModel.insertAllConversation(getAllSms(query))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val myPackageName = packageName
                if (Telephony.Sms.getDefaultSmsPackage(this) == myPackageName) {
                    isDefault = true
                    buildSmsRecyclerView()
                    refreshConversationList(null)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun deleteItem(position: Int) {
        mRecentlyDeletedItem = convoList[position]
        mRecentlyDeletedItemPosition = position
        convoList.removeAt(position)
        adapter.notifyItemRemoved(position)
        Log.d("TAG", "deleteItem: ${convoList.size}")
        showUndoSnackbar()
    }

    private fun showUndoSnackbar() {
        val view = findViewById<DrawerLayout>(R.id.drawerLayout)
        val snackbar = Snackbar.make(
            view, "Conversation archived...",
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

    private fun buildSmsRecyclerView() {

        recyclerView.setHasFixedSize(true)
        val lm = LinearLayoutManager(this)
        lm.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = lm
        adapter = ConversationAdapter(this, convoList)
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter

        adapter.setItemSwipeListener(object : OnSwipeLisetener {
            override fun onSwipeLeft(position: Int) {

                deleteItem(position)
            }

            override fun onSwipeRight(position: Int) {
                deleteItem(position)

            }
        })

        adapter.setItemClickListener(object : ItemCLickListener {
            override fun itemClicked(
                color: Int,
                contact: String,
                contactName: String?,
                id: String,
                threadId: String
            ) {
                val intent = Intent(this@MainActivity, ComposeActivity::class.java)
                intent.putExtra(ADDRESS, contact)
                intent.putExtra(CONTACTNAME, contactName)
                intent.putExtra(THREADID, threadId)
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

                showDialog(
                    "",
                    "Are you sure you want to delete ${contactName ?: contact} conversation?",
                    "Yes",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                        conversationViewModel.deleteConversation(threadId, position)
//                        deleteSMS(this@MainActivity, contact)
//                        refreshConversationList(null)
                    },
                    "No",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }, false
                )


            }
        })
    }


    fun deleteSMS(context: Context, number: String) {
        try {

            val uriSms = Uri.parse("content://sms/inbox")
            val c = context.contentResolver.query(
                uriSms,
                arrayOf("_id", "thread_id", "address", "person", "date", "body"), null, null, null
            )

            if (c != null && c.moveToFirst()) {
                do {
                    val id = c.getLong(0)
                    val address = c.getString(2)
                    if (address == number) {

                        context.contentResolver.delete(
                            Uri.parse("content://sms/$id"), null, null
                        )
                    }
                } while (c.moveToNext())
            }
            c?.close()
        } catch (e: Exception) {

        } finally {
        }


    }


    private fun getAllSms(filter: String? = null): ArrayList<Conversation> {

        val lstSms = ArrayList<Conversation>()
        var selectionArgs: Array<String>? = null
        var selection: String? = null

        if (!filter.isNullOrEmpty()) {
            selection = SmsContract.SMS_SELECTION_SEARCH
            selectionArgs = arrayOf("%$filter%", "%$filter%")
        }

        val cr = this.contentResolver
        val c = cr.query(ALL_SMS_URI, null, selection, selectionArgs, SmsContract.SORT_DESC)
        val totalSMS = c!!.count

        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val objSms = Conversation()
                objSms.id = c.getString(c.getColumnIndexOrThrow("_id"))
                //number of conversation
                objSms.address = c.getString(c.getColumnIndexOrThrow(ADDRESS))
                objSms.msg = c.getString(c.getColumnIndexOrThrow(SmsContract.BODY))
                objSms.threadId = c.getString(c.getColumnIndexOrThrow(THREADID))
                //1 if msg is read
                objSms.readState = c.getString(c.getColumnIndex(READ))
                objSms.time = c.getString(c.getColumnIndexOrThrow(DATE))

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.folderName = "inbox"
                } else {
                    objSms.folderName = "sent"
                }

                if (objSms.address != null)
                    lstSms.add(objSms)

                c.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }

        c.close()


        for (i in lstSms.indices) {
            if (lstSms[i].address != null) {
                if (lstSms[i].address!!.contains("+92")) {
                    lstSms[i].address = lstSms[i].address!!.replace("+92", "0")
                }
            }
        }

        return ArrayList(LinkedHashSet<Conversation>(lstSms))

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

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
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {

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
                                finish()
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
                                    Uri.fromParts("package", packageName, null)
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            },
                            "No, Exit app",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                                finish()
                            }, false
                        )
                    }


                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setCancelable(isCancelable).setMessage(msg)
        builder.setPositiveButton(positiveLabel, positiveClickButton)
        builder.setNegativeButton(negativeLabel, negativeClickButton)
        val alert = builder.create()
        alert.show()
        return alert
    }


}
