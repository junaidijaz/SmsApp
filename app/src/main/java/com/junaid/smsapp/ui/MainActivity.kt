package com.junaid.smsapp.ui

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.junaid.smsapp.R
import com.junaid.smsapp.adapters.ConversationAdapter
import com.junaid.smsapp.adapters.ItemCLickListener
import com.junaid.smsapp.model.Conversation
import com.junaid.smsapp.utils.SmsContract
import com.junaid.smsapp.utils.SmsContract.Companion.ADDRESS
import com.junaid.smsapp.utils.SmsContract.Companion.ALL_SMS_URI
import com.junaid.smsapp.utils.SmsContract.Companion.CONTACTNAME
import com.junaid.smsapp.utils.SmsContract.Companion.DATE
import com.junaid.smsapp.utils.SmsContract.Companion.READ
import com.junaid.smsapp.utils.SmsContract.Companion.THREADID
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    private val appPermissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )


    private val PERMISSION_REQUEST_CODE = 1240

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**check for app permissions
         * in case one or more permissions are not granted
         * activityCompact.requestPermissions will request permissions
         * and the control goes to onRequestPermissionsResult() callback method
         **/
        checkAndRequestPermissions()

        recyclerView.setHasFixedSize(true)
        val lm = LinearLayoutManager(this)
        lm.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = lm

        setInboxTextChangesLis()
    }

    /**
     *this function will change
     */
    private fun setInboxTextChangesLis() {

        toolbarSearch.addTextChangedListener {
            buildSmsRecyclerView(getAllSms(it.toString()))
        }

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
        } else {
            buildSmsRecyclerView(getAllSms())
        }


        //app has all permissions
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val myPackageName = packageName
                if (Telephony.Sms.getDefaultSmsPackage(this) == myPackageName) {
                    buildSmsRecyclerView(getAllSms())
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun buildSmsRecyclerView(allSms: List<Conversation>) {


        val s = LinkedHashSet<Conversation>(allSms)
        val data = ArrayList<Conversation>(s)
        val adapter = ConversationAdapter(this, ArrayList(data))
        recyclerView.adapter = adapter

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
        })
    }


    private fun getAllSms(filter: String? = null): List<Conversation> {

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
                objSms.threadId = c.getString(c.getColumnIndexOrThrow(SmsContract.THREADID))
                //1 if msg is read
                objSms.readState = c.getString(c.getColumnIndex(READ))
                objSms.time = c.getString(c.getColumnIndexOrThrow(DATE))

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.folderName = "inbox"
                } else {
                    objSms.folderName = "sent"
                }

                lstSms.add(objSms)
                c.moveToNext()
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }

        c.close()

        for (i in lstSms.indices) {
            if (lstSms[i].address!!.contains("+92")) {
                lstSms[i].address = lstSms[i].address!!.replace("+92", "0")
            }
        }

        return lstSms
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
