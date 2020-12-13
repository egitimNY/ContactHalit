package com.halit.contacthalit

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_record_detail.*
import java.util.*

class RecordDetailActivity : AppCompatActivity() {

    private val REQUEST_PHONE_CALL: Int= 1

    // actionBar
    private var actionBar: ActionBar?=null

    // dbHelper
    private var dbHelper:MyDbHelper?=null

    private  var recordId:String? = null

    private var number = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        // setting up actionBar
        actionBar = supportActionBar
        actionBar!!.title = "Record Detail"
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar!!.setDefaultDisplayHomeAsUpEnabled(true)

        // init dbHelper
        dbHelper = MyDbHelper(this)

        // get record id from intent
        val intent = intent
        recordId = intent.getStringExtra("RECORD_ID")

        showRecordDetail()

    }

    private fun showRecordDetail() {
        // get record details

        val selectQuery = "SELECT * FROM "+Constants.TABLE_NAME+ " WHERE "+ Constants.C_ID + " ="+recordId+""

//        val db = dbHelper!!.writableDatabase
        val db = dbHelper!!.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {

                val id = ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID))
                val name = ""+cursor.getString(cursor.getColumnIndex(Constants.C_NAME))
                val image = ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE))
                val bio = ""+cursor.getString(cursor.getColumnIndex(Constants.C_BIO))
                val phone = ""+cursor.getString(cursor.getColumnIndex(Constants.C_PHONE))
                val email = ""+cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL))
                val dob = ""+cursor.getString(cursor.getColumnIndex(Constants.C_DOB))
                val addedTimeStamp = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP))
                val updatedTimeStamp = ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))

                // convert timeStamp to dd//mm/yyyy e.g. 14/01/2020 08:22 AM
                val calendar1 = Calendar.getInstance(Locale.getDefault())
                calendar1.timeInMillis = addedTimeStamp.toLong()
                val  timeAdded = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar1)

                val calendar2 = Calendar.getInstance(Locale.getDefault())
                calendar1.timeInMillis = updatedTimeStamp.toLong()
                val  timeUpdated = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar2)


                // set data
                nameTv.text = name
                bioTv.text = bio
                phoneTv.text = phone
                emailTv.text = email
                dobTv.text = dob
                addedDateTv.text = timeAdded
                updatedDateTv.text = timeUpdated

                main_item_contact.setOnClickListener {
                    makeACall(phone)
                }

                main_item_detail_sms.setOnClickListener {
                    sendSMS(phone)
                }

                main_item_row_email.setOnClickListener {
                    sendEmail()
                }

                profileIv.setOnClickListener {

                    // pass id to next activity to show record
                    val intent = Intent(this@RecordDetailActivity, BigImageActivity::class.java)
                    intent.putExtra("RECORD_ID", recordId)
                    startActivity(intent)


//                        val intent = Intent(this@RecordDetailActivity, BigImageActivity::class.java)
//                        intent.putExtra("resId", image)
//                        startActivity(intent)

                }

                // if user dosn't attach image then imageUri will be null, so set default image in that case
                if (image == "null"){
                    // no image in record, set default
                    profileIv.setImageResource(R.drawable.ic_person_black)
                }else{
                    // have image in record
                    profileIv.setImageURI(Uri.parse(image))
                }

            }while (cursor.moveToNext())
        }
        // close db connection
        db.close()
    }

    private fun sendSMS(phone: String) {
        startActivity(Intent(Intent.ACTION_SENDTO).apply {
            type = "vnd.android-dir/mms-sms"
            data = Uri.parse("sms:$phone")
            putExtra("sms_body", "dummy body")
        })
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            /*
            putExtra(Intent.EXTRA_EMAIL, arrayOf("info@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Hello There")
            putExtra(Intent.EXTRA_TEXT, "Add Message here")
            */

            putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
            putExtra(Intent.EXTRA_SUBJECT, "")
            putExtra(Intent.EXTRA_TEXT, "")

            type = "message/rfc822"
        }

        try {
            startActivity(Intent.createChooser(emailIntent,
                "Send email using..."));
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this,
                "No email clients installed.",
                Toast.LENGTH_SHORT).show();
        }
    }

    private fun makeACall(number: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            this@RecordDetailActivity.number = number
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),REQUEST_PHONE_CALL)
        } else{
            startCall(number)
        }
    }

    private fun startCall(number: String) {
        val intent =
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        startActivity(intent)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PHONE_CALL){
            if(permissions[0]== Manifest.permission.CALL_PHONE){
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    startCall(number)
                }
            }
        }
    }

}
