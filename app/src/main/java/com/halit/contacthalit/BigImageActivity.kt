package com.halit.contacthalit

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_big_image.*

class BigImageActivity : AppCompatActivity() {

    // dbHelper
    private var dbHelper:MyDbHelper?=null

    private  var recordId:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)


        // get record id from intent
        val intent = intent
        recordId = intent.extras?.getString("RECORD_ID")


        val selectQuery = "SELECT * FROM "+Constants.TABLE_NAME+ " WHERE "+ Constants.C_ID + " ="+recordId+""

//        val db = dbHelper!!.writableDatabase
        dbHelper = MyDbHelper(this)
        val db = dbHelper?.writableDatabase
        val cursor = db?.rawQuery(selectQuery, null)

        if (cursor?.moveToFirst() == true) {
            val image = ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE))
            if(image != "null") {
                imageIV.setImageURI(Uri.parse(image))
            } else {
                imageIV.setImageResource(R.drawable.ic_person_black)
            }
        } else {
            imageIV.setImageResource(R.drawable.ic_person_black)
        }

    }

}
