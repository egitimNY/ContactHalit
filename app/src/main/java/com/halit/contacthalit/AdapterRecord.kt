package com.halit.contacthalit

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for recyclerView
class AdapterRecord(val makeACall: (number: String) -> Unit): RecyclerView.Adapter<AdapterRecord.HolderRecord>(){

    private var context:Context?=null
    private var recordList:ArrayList<ModelRecord>?=null

    lateinit var dbHelper:MyDbHelper


    constructor(context: Context?, recordList: ArrayList<ModelRecord>?, makeACall: (number: String) -> Unit) : this(makeACall) {
        this.context = context
        this.recordList = recordList

        dbHelper = MyDbHelper(context)
    }

    fun clearRecords() {
        recordList?.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecord {
        // inflate the layout row_record.xml
        return HolderRecord(
            LayoutInflater.from(context).inflate(R.layout.row_record, parent, false)
        )
    }

    override fun getItemCount(): Int {
        // return items/records/list size
        return recordList!!.size
    }

    override fun onBindViewHolder(holder: HolderRecord, position: Int) {
        // get data, set data, handle clicks

        //get data
        val model = recordList!!.get(position)

        val id = model.id
        val name = model.name
        val image = model.image
        val bio = model.bio
        val phone = model.phone
        val email = model.email
        val dob = model.dob
        val addedTime = model.addedTime
        val updatedTime = model.updatedTime

        // set data to views
        holder.nameIv.text = name
        holder.phoneTv.text = phone
        holder.emailTv.text = email
        holder.dobTv.text = dob
        // if user dosn't attach image then imageUri will be null, so set default image in that case
        if (image == "null"){
            // no image in record, set default
            holder.profileIv.setImageResource(R.drawable.ic_person_black)
        }else{
            // have image in record
            holder.profileIv.setImageURI(Uri.parse(image))
        }

        // show record in new activity on clicking record
        holder.itemView.setOnClickListener {
            // pass id to next activity to show record
            val intent = Intent(context, RecordDetailActivity::class.java)
            intent.putExtra("RECORD_ID", id)
            context!!.startActivity(intent)

        }

        holder.main_item_contact.setOnClickListener {
            makeACall(model.phone)
        }

        // handle more button click: show delete/edit options
        holder.morebtn.setOnClickListener {
            // show more options e.g. edit, delete
            showMoreOptions(
                position,
                id,
                name,
                phone,
                email,
                dob,
                bio,
                image,
                addedTime,
                updatedTime
            )
        }

    }

    private fun showMoreOptions(
        position: Int,
        id: String,
        name: String,
        phone: String,
        email: String,
        dob: String,
        bio: String,
        image: String,
        addedTime: String,
        updatedTime: String
    ) {
        // options to display in dialog
        val options = arrayOf("Edit", "Delete")
        // dialog
        val dialog:AlertDialog.Builder = AlertDialog.Builder(context)
        // set items and clickListener
        dialog.setItems(options) { dialog, which ->
            // handle item clicks
            if (which==0){
                // edit clicked
                val intent = Intent(context, AddUpdateRecordActivity::class.java)
                intent.putExtra("ID", id)
                intent.putExtra("NAME", name)
                intent.putExtra("PHONE", phone)
                intent.putExtra("EMAIL", email)
                intent.putExtra("DOB", dob)
                intent.putExtra("BIO", bio)
                intent.putExtra("IMAGE", image)
                intent.putExtra("ADDED_TIME", addedTime)
                intent.putExtra("UPDATED_TIME", updatedTime)
                intent.putExtra("isEditMode", true) // want to update existing record, set it true
                context!!.startActivity(intent)
            }else{
                // delete clicked

                dbHelper.deleteRecord(id)
                // Refresh record by calling Activity's onResume method
                (context as MainActivity)!!.onResume()

            }

        }
        // show dialog
        dialog.show()

    }

    inner class HolderRecord(itemView: View): RecyclerView.ViewHolder(itemView) {

        // Views from Row_record.xml
        var profileIv:ImageView = itemView.findViewById(R.id.profileIv)
        var nameIv:TextView = itemView.findViewById(R.id.nameTv)
        var phoneTv:TextView = itemView.findViewById(R.id.phoneTv)
        var emailTv:TextView = itemView.findViewById(R.id.emailTv)
        var dobTv:TextView = itemView.findViewById(R.id.dobTv)
        var morebtn:ImageButton = itemView.findViewById(R.id.moreBtn)

        var main_item_contact: ImageView = itemView.findViewById(R.id.main_item_contact)
    }


}