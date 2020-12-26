package com.halit.contacthalit

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// database helper class that contains all CRUD method
class MyDbHelper (context: Context?):SQLiteOpenHelper(
    context,
    Constants.DB_NAME,
    null,
    Constants.DB_VERSION
){
    override fun onCreate(db: SQLiteDatabase) {
        // create table on that db
        db.execSQL(Constants.CREATE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // upgrade database(if there is any structure change, change db version)
        // drop older table if exists

        db.execSQL("DROP TABLE IF EXISTS"+ Constants.TABLE_NAME)
    }

    // insert record to db
    fun insertRecord(
        name:String?,
        image:String?,
        bio:String?,
        phone:String?,
        email:String?,
        dob:String?,
        addedTime:String?,
        updatedTime:String?,
        createdBy: String?
    ):Long{
        // get writeable database because we want to write data
        val db = this.writableDatabase
        val values = ContentValues()
        // id will be inserted automatically as we set AUTOINCREMENT in query
        // insert data
        values.put(Constants.C_NAME, name)
        values.put(Constants.C_IMAGE, image)
        values.put(Constants.C_BIO, bio)
        values.put(Constants.C_PHONE, phone)
        values.put(Constants.C_EMAIL, email)
        values.put(Constants.C_DOB, dob)
        values.put(Constants.C_ADDED_TIMESTAMP, addedTime)
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTime)
        values.put(Constants.C_CREATED_BY, createdBy)

        // insert row, it will return record id of saved record
        val id = db.insert(Constants.TABLE_NAME, null, values)
        // close db connection
        db.close()
        // return id of inserted record
        return id
    }

    // update record to db
    fun updateRecord(id:String,
                     name: String?,
                     image: String?,
                     bio: String?,
                     phone: String?,
                     email: String?,
                     dob: String?,
                     addedTime: String?,
                     updatedTime: String?,
                     createdBy: String?):Long
     {
        // get writeAble database
        val db = this.writableDatabase
        val values = ContentValues()
        // id will be inserted automatically as we set AUTOINCREMENT in query
        // insert data
        values.put(Constants.C_NAME, name)
        values.put(Constants.C_IMAGE, image)
        values.put(Constants.C_BIO, bio)
        values.put(Constants.C_PHONE, phone)
        values.put(Constants.C_EMAIL, email)
        values.put(Constants.C_DOB, dob)
        values.put(Constants.C_ADDED_TIMESTAMP, addedTime)
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTime)
        values.put(Constants.C_CREATED_BY, createdBy)

         // update
         return db.update(Constants.TABLE_NAME,
             values,
             "${Constants.C_ID}=? and ${Constants.C_CREATED_BY}=?",
             arrayOf(id,"'$createdBy'")).toLong()
    }

    // get all data
    fun getAllRecords(orderBy:String, createdBy: String?):ArrayList<ModelRecord>{
        // orderBy query will allow to sort data e.g. newest/oldest first, name ascending/descending
        // it will return list or record since we have used return type ArrayList<ModelRecord>
        val recordList = ArrayList<ModelRecord>()
        // query to select all records
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_CREATED_BY}='$createdBy' ORDER BY $orderBy"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
            // looping through all record and to list
        if (cursor.moveToFirst()){
            do {
                val modelRecord = ModelRecord(
                    ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_NAME)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_BIO)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_PHONE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_DOB)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                )
                // add record to list
                recordList.add(modelRecord)
            }while (cursor.moveToNext())
        }
        // db close connection
        db.close()
        // return the queried result list
        return recordList
    }

    // search data
    fun searchRecords(query: String, createdBy: String?): ArrayList<ModelRecord>{
        // it will return list or record since we have used return type ArrayList<ModelRecord>
        val recordList = ArrayList<ModelRecord>()
        // query to select all records
        val selectQuery =
            "SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_CREATED_BY}='$createdBy' AND ${Constants.C_NAME} LIKE '%$query%'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        // looping through all record and to list
        if (cursor.moveToFirst()){
            do {
                val modelRecord = ModelRecord(
                    ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_NAME)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_BIO)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_PHONE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_DOB)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                )
                // add record to list
                recordList.add(modelRecord)
            }while (cursor.moveToNext())
        }
        // db close connection
        db.close()
        // return the queried result list
        return recordList
    }

    // Get total number of record
    fun recordCount():Int{
        val countQuery = "SELECT * FROM ${Constants.TABLE_NAME}"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    // Delete (single) record using record ID   (Halit Changed)
    fun deleteRecord(id: String, string: String?){
        val  db = writableDatabase
        db.delete(
            Constants.TABLE_NAME,
            "${Constants.C_ID} =?",
            arrayOf(id)
        )
        db.close()
    }

      // Delete (single) record using record ID  (Sagar Changed)
/*    fun deleteRecord(id: String, createdBy: String?){
        val  db = writableDatabase
        db.delete(
            Constants.TABLE_NAME,
            "${Constants.C_ID} =? and ${Constants.C_CREATED_BY}=?",
            arrayOf(id, "'$createdBy'")
        )
        db.close()
    }*/

    // Delete all records
    fun deleteAllRecords(createdBy: String?){
        val db = writableDatabase
        db.execSQL("DELETE FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_CREATED_BY}='$createdBy'")
        db.close()
    }
}