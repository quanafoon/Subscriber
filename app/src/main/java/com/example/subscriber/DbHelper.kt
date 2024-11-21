package com.example.subscriber

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DB_NAME = "location_tracker.db"
const val DB_VERSION = 1

class DbHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create the Location table
        val createLocationTableQuery = ("CREATE TABLE Location (" +
                "studentID TEXT PRIMARY KEY," +  // Unique identifier for the student
                "lat REAL," +                   // Latitude as a floating-point number
                "lng REAL," +                   // Longitude as a floating-point number
                "timestamp INTEGER," +          // Timestamp as an integer (epoch time in milliseconds)
                "speed REAL)")                  // Speed as a floating-point number

        db.execSQL(createLocationTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For now, simply drop and recreate the table (non-destructive migration should be implemented in the future)
        db.execSQL("DROP TABLE IF EXISTS Location")
        onCreate(db)
    }


    fun insertLocation(studentID: String, lat: Double, lng: Double, timestamp: Long, speed: Double) {
        val values = ContentValues().apply {
            put("studentID", studentID)
            put("lat", lat)
            put("lng", lng)
            put("timestamp", timestamp)
            put("speed", speed)
        }

        val db = this.writableDatabase
        db.insert("Location", null, values)
        db.close()
    }


    fun getAllLocations(): List<Location> {
        val result: MutableList<Location> = mutableListOf()

        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Location", null)

        if (cursor.moveToFirst()) {
            do {
                val studentIDIdx = cursor.getColumnIndex("studentID")
                val latIdx = cursor.getColumnIndex("lat")
                val lngIdx = cursor.getColumnIndex("lng")
                val timestampIdx = cursor.getColumnIndex("timestamp")
                val speedIdx = cursor.getColumnIndex("speed")

                if (studentIDIdx >= 0 && latIdx >= 0 && lngIdx >= 0 && timestampIdx >= 0 && speedIdx >= 0) {
                    val studentID = cursor.getString(studentIDIdx)
                    val lat = cursor.getDouble(latIdx)
                    val lng = cursor.getDouble(lngIdx)
                    val timestamp = cursor.getLong(timestampIdx)
                    val speed = cursor.getDouble(speedIdx)

                    result.add(Location(studentID, lat, lng, timestamp, speed))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return result
    }


    fun deleteLocation(studentID: String) {
        val db = this.writableDatabase
        db.delete("Location", "studentID = ?", arrayOf(studentID))
        db.close()
    }


    fun updateLocation(studentID: String, lat: Double, lng: Double, timestamp: Long, speed: Double) {
        val values = ContentValues().apply {
            put("lat", lat)
            put("lng", lng)
            put("timestamp", timestamp)
            put("speed", speed)
        }

        val db = this.writableDatabase
        db.update("Location", values, "studentID = ?", arrayOf(studentID))
        db.close()
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM Location")
        db.close()
    }
}