package com.halit.contacthalit

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_update_record.*

class AddUpdateRecordActivity : AppCompatActivity() {

    // permission constants
    private val CAMERA_REQUEST_CODE = 100;
    private val STORAGE_REQUEST_CODE = 101;
    // image pick constants
    private val IMAGE_PICK_CAMERA_CODE = 102;
    private val IMAGE_PICK_GALLERY_CODE = 103;
    // array of permissions
    private lateinit var  cameraPermissions:Array<String> // camera and storage
    private lateinit var  storagePermission:Array<String>  // only storage
    // variables that will contain data to save in database
    private var imageUri: Uri? = null
    private var id:String? =""
    private var name:String? =""
    private var phone:String? =""
    private var email:String? =""
    private var dob:String? =""
    private var bio:String? =""
    private var addedTime:String? =""
    private var updatedTime:String? =""

    private var isEditMode = false

    private lateinit var sharedPreferences: SharedPreferences
    private var userName: String? = null


    // actionBar
    private var actionBar: ActionBar? = null

    lateinit var dbHelper:MyDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update_record)
        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userName = sharedPreferences.getString("userName","") ?: ""
        //init actionbar
        actionBar = supportActionBar
        //title of actionbar
        actionBar!!.title = "AddRecord"
        //back button in actionbar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        // get data from intent
        val intent = intent
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode){
            // editing data, came here from adapter
            actionBar!!.title = "Update Record"

            id = intent.getStringExtra("ID")
            name = intent.getStringExtra("NAME")
            phone = intent.getStringExtra("PHONE")
            email = intent.getStringExtra("EMAIL")
            dob = intent.getStringExtra("DOB")
            bio = intent.getStringExtra("BIO")
            imageUri = Uri.parse(intent.getStringExtra("IMAGE"))
            addedTime = intent.getStringExtra("ADDED_TIME")
            updatedTime = intent.getStringExtra("UPDATED_TIME")

            // -------Set Data to views------
            /* if user did not attached image while saving record then
            * imageUri value be "NULL", so set default image */
            if (imageUri.toString() == "null"){
                // no image
                profileIv.setImageResource(R.drawable.ic_person_black)
            }else{
                // have image
                profileIv.setImageURI(imageUri)
            }
            nameEt.setText(name)
            phoneEt.setText(phone)
            emailEt.setText(email)
            dobEt.setText(dob)
            biolEt.setText(bio)
        }
        else{
            // adding new data, came here from MainActivity
            actionBar!!.title = "Add Record"
        }

        // init DB helper class
        dbHelper = MyDbHelper(this)

        // init permissions array
        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // click imageView to pick image
        profileIv.setOnClickListener {
            // show image pick dialog
            imagePickDialog();

        }

        saveBtn.setOnClickListener {
            inputData();
        }
    }

    private fun inputData() {
        // get data
        name = "" + nameEt.text.toString().trim()
        phone = "" + phoneEt.text.toString().trim()
        email = "" + emailEt.text.toString().trim()
        dob = "" + dobEt.text.toString().trim()
        bio = "" + biolEt.text.toString().trim()

        if (isEditMode){
            // editing

            val timeStamp = "${System.currentTimeMillis()}"
            dbHelper?.updateRecord(
                "$id",
                "$name",
                "$imageUri",
                "$bio",
                "$phone",
                "$email",
                "$dob",
                "$addedTime",
                timeStamp
            )
            Toast.makeText(this,"Updated...", Toast.LENGTH_SHORT).show()

        }else{
            // adding new

            // save data to DB
            val timeStamp = System.currentTimeMillis()
            val id = dbHelper.insertRecord(
                ""+name,
                ""+imageUri,
                ""+bio,
                ""+phone,
                ""+email,
                ""+dob,
                "$timeStamp",
                "$timeStamp",
                userName
            )
            Toast.makeText(this,"Record Added against ID $id", Toast.LENGTH_SHORT).show()
        }


    }

    private fun imagePickDialog() {
        // Options to display in dialog
        val options = arrayOf("Camera", "Gallery")
        // dialog
        val builder = AlertDialog.Builder(this)
        // Title
        builder.setTitle("Pick Image From")
        // Set Items/ Options
        builder.setItems(options) { dialog, which ->
            // handle item clicks
            if (which == 0) {
                // Camera clicked
                if (!checkCameraPermissions()) {
                    // permissions not granted
                    requestCameraPermission()
                } else {
                    // permissions already granted
                    pickFromCamera()
                }
            }
            else {
                // gallery clicked
                if (!checkStoragePermission()) {
                    // permissions not granted
                    requestStoragePermission()
                } else {
                    // permissions already granted
                    pickFromGallery()
                }
            }
        }
        // Show dialog
        builder.show()

    }

    private fun pickFromGallery() {
        // pick image from gallery using Intent
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*" // only image to be picked
        startActivityForResult(
            galleryIntent,
            IMAGE_PICK_GALLERY_CODE
        )
    }

    private fun requestStoragePermission() {
        // request the storage permission
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        // check if storage permission is enabled or not
        return  ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickFromCamera() {
        // pick image from camera using Intent
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Image Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Description")
        // put image uri
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // intent to open camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(
            cameraIntent,
            IMAGE_PICK_CAMERA_CODE
        )

    }

    private fun requestCameraPermission() {
        // request the camera permissions
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermissions(): Boolean {
        // check if camera permissions (camera and storage) are enamed or not
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        return  result && result1

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go back to previous activity
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    // if allowed returns tru otherwise false
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera()
                    }else{
                        Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            STORAGE_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    // if allowed returns true otherwise false
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted){
                        pickFromGallery()
                    }else{
                        Toast.makeText(this, "Storage permissions is required", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // image picked from camera or gallery will be received here
        if (resultCode == Activity.RESULT_OK){
            // image picked
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                // picked from gallery
                // crop image
                CropImage.activity(data!!.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this)
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // picked from camera
                // crop image
                CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this)
            }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                // cropped image received
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK){
                    val resultUri = result.uri
                    imageUri = resultUri
                    // set image
                    profileIv.setImageURI(resultUri)
                }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    // error
                    val error = result.error
                    Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
