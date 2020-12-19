package com.halit.contacthalit

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * This class is responsible for displaying all the available records in the DB, and
 * you could edit and delete the existing contacts by selecting a three dots icon
 * and you can go to AddUpdateRecord screen to create a new record
 */
class MainActivity : AppCompatActivity() {

    //Request code while requesting the call permission from the user
    private val REQUEST_PHONE_CALL: Int = 1
    // dbHelper
    lateinit var dbHelper: MyDbHelper

    // orderBy / sort queries
    private val NEWEST_FIRST = "${Constants.C_ADDED_TIMESTAMP} DESC"
    private val OLDEST_FIRST = "${Constants.C_ADDED_TIMESTAMP} ASC"
    private val TITLE_ASC = "${Constants.C_NAME} ASC"
    private val TITLE_DESC = "${Constants.C_NAME} DESC"

    private var recentSortOrder = NEWEST_FIRST

    //----for permission----
    private val STORAGE_REQUEST_CODE_EXPORT = 1
    private val STORAGE_REQUEST_CODE_IMPORT = 2
    private lateinit var storagePermission: Array<String>

    //reference variable of adapter to display a list of values in the recycler view
    lateinit var adapterRecord: AdapterRecord
    //A variable to hold the phone number until user grants the permission to make a call
    private var number: String = ""
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("loggedIn",true).apply()

        // init array of permission
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val preferences = getSharedPreferences("PREFS", 0)
        val ifShowDialog = preferences.getBoolean("showDialog", true)
        if (ifShowDialog) {
            showDialog()
        }


        // init dbHelper
        dbHelper = MyDbHelper(this)

        loadRecords(NEWEST_FIRST) // by default load newest first

        // click FloatingActionButton to start AddUpdateRecordActivity
        addRecordBtn.setOnClickListener {
            val intent = Intent(this, AddUpdateRecordActivity::class.java)
            intent.putExtra("isEditMode", false) // want to add new record, set it false
            startActivity(intent)
        }
    }

    private fun checkStoragePermission(): Boolean {
        // check if the storage permission is allowed or not and return result as true (allowed) / false (not allowed)
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermissionImport() {
        // request storage permission for import
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_IMPORT)
    }

    private fun requestStoragePermissionExport() {
        // request storage permission for export
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_EXPORT)
    }

    fun exportCSV() {
        // path of csv file

        val folder =
//          File(Environment.getExternalStorageDirectory().toString() +"/"+ "SQLiteBackupKotlin")
            File("${Environment.getExternalStorageDirectory()}/SQLiteBackupKotlin")

        var isFolderCreated = false
        if (!folder.exists()) isFolderCreated = folder.mkdir()

        // file name
        val csvFileName = "SQLite_Backup.csv"

        // file name and path
        val fileNameAndPath = "$folder/$csvFileName"

        // get records to save in backup
        var recordList = ArrayList<ModelRecord>()
        recordList.clear()
        recordList = dbHelper.getAllRecords(OLDEST_FIRST)

        try {
            val fw = FileWriter(fileNameAndPath)
            for (i in recordList.indices) {
                fw.append("" + recordList[i].id) // id
                fw.append(",")
                fw.append("" + recordList[i].name) // name
                fw.append(",")
                fw.append("" + recordList[i].image) // image
                fw.append(",")
                fw.append("" + recordList[i].bio) // bio
                fw.append(",")
                fw.append("" + recordList[i].phone) // phone
                fw.append(",")
                fw.append("" + recordList[i].email) // email
                fw.append(",")
                fw.append("" + recordList[i].dob) // dob
                fw.append(",")
                fw.append("" + recordList[i].addedTime) // added time
                fw.append(",")
                fw.append("" + recordList[i].updatedTime) // updated time
                fw.append("\n")
            }
            fw.flush()
            fw.close()


            Toast.makeText(this, "Backup Exported to $fileNameAndPath...", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {

            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun importCSV() {
        // complete path of csv
        val filePathAndName =
            "${Environment.getExternalStorageDirectory()}/SQLiteBackupKotlin/SQLite_Backup.csv"

        val csfFile = File(filePathAndName)

        // check if backup file exist or not
        if (csfFile.exists()) {
            // exists
            try {
                val csvReader = CSVReader(FileReader(csfFile.absolutePath))
                var nextLine: Array<String>
                while (csvReader.readNext().also { nextLine = it } != null) {
                    // get record from csv
                    val idd = nextLine[0]
                    val name = nextLine[1]
                    val image = nextLine[2]
                    val bio = nextLine[3]
                    val phone = nextLine[4]
                    val email = nextLine[5]
                    val dob = nextLine[6]
                    val addedTime = nextLine[7]
                    val updatedTime = nextLine[8]

                    // add to db
                    val timestamp = System.currentTimeMillis()
                    val id = dbHelper.inserRecord(
                        "" + name,
                        "" + image,
                        "" + bio,
                        "" + phone,
                        "" + email,
                        "" + dob,
                        "$timestamp",
                        "$timestamp"
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Backup not found", Toast.LENGTH_LONG).show()
        }

    }

    private fun showDialog() {
        val alertDialogBuilder =
            androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder
            .setTitle("Disclaimer")
            .setMessage("The information provided in app though is compiled with utmost care,the app can not guarantee that this information is and stays 100% accurate. We therefore reserve all right of app and accept no liability for any kind of damage directly or indirectly that can come from the use or not able to use the information and functionality provided by app.")
            .setPositiveButton(
                "I Don't Accept"
            ) { dialog, which ->
                //                    dialog.dismiss();
                finish()
            }
            .setNeutralButton(
                "I Accept"
            ) { dialog, which ->
                dialog.dismiss()
                val preferences = getSharedPreferences("PREFS", 0)
                val editor = preferences.edit()
                editor.putBoolean("showDialog", false)
                editor.apply()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    private fun loadRecords(orderBy: String) {
        recentSortOrder = orderBy
        adapterRecord = AdapterRecord(
            this,
            dbHelper.getAllRecords(orderBy)
        ) { type: AdapterRecord.Action, data: String ->
            handleAction(type, data)
        }

        recordRv.adapter = adapterRecord
    }

    private fun searchRecords(query: String) {
        adapterRecord = AdapterRecord(
            this,
            dbHelper.searchRecords(query)
        ) { type: AdapterRecord.Action, data: String ->
            handleAction(type, data)
        }

        recordRv.adapter = adapterRecord
    }

    private fun handleAction(type: AdapterRecord.Action, data: String) {
        when (type) {
            AdapterRecord.Action.SMS -> sendSMS(data)
            AdapterRecord.Action.PHONE -> makeACall(data)
            AdapterRecord.Action.EMAIL -> sendEmail(data)
        }
    }

    private fun makeACall(number: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this@MainActivity.number = number
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PHONE_CALL
            )
        } else {
            startCall(number)
        }
    }

    private fun startCall(number: String) {
        val intent =
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun sendSMS(phone: String) {
        startActivity(Intent(Intent.ACTION_SENDTO).apply {
            type = "vnd.android-dir/mms-sms"
            data = Uri.parse("sms:$phone")
            putExtra("sms_body", "dummy body")
        })
    }

    private fun sendEmail(email: String) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain";
            putExtra(Intent.EXTRA_EMAIL, email)
            putExtra(Intent.EXTRA_SUBJECT, "")
            putExtra(Intent.EXTRA_TEXT, "")
//            putExtra(Intent.EXTRA_SUBJECT, "Hello There")
//            putExtra(Intent.EXTRA_TEXT, "Add Message here")

            type = "message/rfc822"
        }

        try {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    "Send email using..."
                )
            );
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No email clients installed.",
                Toast.LENGTH_SHORT
            ).show();
        }
    }

    private fun sortDialog() {
        // OPTIONS to display in dialog
        val options = arrayOf("Name Ascending", "Name Descending", "Newest", "Oldest")
        // Dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setItems(options) { _, which ->
            // Handle Items
            if (which == 0) {
                // Name Ascending
                loadRecords(TITLE_ASC)
            } else if (which == 1) {
                // Name Descending
                loadRecords(TITLE_DESC)
            } else if (which == 2) {
                // Newest
                loadRecords(NEWEST_FIRST)
            } else if (which == 3) {
                // Oldest first
                loadRecords(OLDEST_FIRST)
            }
        }
            .show()
    }

    public override fun onResume() {
        super.onResume()
        loadRecords(recentSortOrder)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate menu
        menuInflater.inflate(R.menu.menu_main, menu)

        // searchView
        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                // search as you type
                if (newText != null) {
                    searchRecords(newText)
                }

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // search when search button on keyboard is clicked
                if (query != null) {
                    searchRecords(query)
                }

                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        val id = item.itemId
        if (id == R.id.action_sort) {
            sortDialog()
        } else if (id == R.id.action_deleteAll) {
            // Delete all records
            val alertDialog = AlertDialog.Builder(this).setTitle("Delete All")
                .setMessage("Do you want to delete All")
                .setPositiveButton("Ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dbHelper.deleteAllRecords()
                        adapterRecord.clearRecords()
                    }
                })
                .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
                .create()
            alertDialog.show()
        } else if (id == R.id.action_backup) {
            // backup all records
            if (checkStoragePermission()) {
                // permission allowed, do backup
                exportCSV()
            } else {
                // permission not allowed, request
                requestStoragePermissionExport()
            }

        } else if (id == R.id.action_restore) {
            // restore all records
            if (checkStoragePermission()) {
                // permission allowed, do restore
                importCSV()
                onResume()

            } else {
                // permission not allowed, request
                requestStoragePermissionImport()

            }

        } else if(id == R.id.action_logout){
            sharedPreferences.edit().putBoolean("loggedIn",false).apply()
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_CALL) {
            if (permissions[0] == Manifest.permission.CALL_PHONE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCall(number)
                }
            }
        }



        when (requestCode) {
            STORAGE_REQUEST_CODE_EXPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission allowed
                    exportCSV()
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_LONG).show()
                }
            }
            STORAGE_REQUEST_CODE_IMPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission allowed
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

}

