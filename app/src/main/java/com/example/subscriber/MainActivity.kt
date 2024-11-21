package com.example.subscriber

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.json.JSONObject
import java.util.UUID


class MainActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    private var client: Mqtt5BlockingClient? = null
    private lateinit var studentIdRecyclerView: RecyclerView
    private lateinit var studentIdAdapter: StudentIdAdapter
    private lateinit var dbHelper: DbHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        connect()
        studentIdRecyclerView = findViewById(R.id.studentIdRecyclerView)
        studentIdRecyclerView.layoutManager = LinearLayoutManager(this)
        studentIdRecyclerView.adapter = studentIdAdapter
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        displayLocations()
    }

    private fun connect() {
        try {
            client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("broker-816038265.sundaebytestt.com")
                .serverPort(1883)
                .build()
                .toBlocking()
            client?.connect()
            Log.i("Subscriber", "Client connected successfully!")

            client?.toAsync()?.subscribeWith()
                ?.topicFilter("locationTracker")
                ?.callback { publish: Mqtt5Publish ->
                    val message = String(publish.payloadAsBytes)
                    Log.i("Subscriber", "Received message: $message")

                    try {
                        val jsonObject = JSONObject(message)
                        val studentID = jsonObject.getString("studentID")
                        val lat = jsonObject.getDouble("latitude")
                        val lng = jsonObject.getDouble("longitude")
                        val timestamp = System.currentTimeMillis() // Use current timestamp
                        val speed = jsonObject.optDouble("speed", 0.0) // Optional speed, default to 0.0

                        val dbHelper = DbHelper(this, null)
                        dbHelper.insertLocation(studentID, lat, lng, timestamp, speed)

                        Log.i("Subscriber", "Inserted into database: $studentID, $lat, $lng, $timestamp, $speed")
                    } catch (e: Exception) {
                        Log.e("Subscriber", "Failed to parse or insert message: ${e.message}")
                    }
                }
                ?.send()

        } catch (e: Exception) {
            Log.e("Subscriber", "Failed to connect or subscribe: ${e.message}")
        }
    }



    private fun displayLocations() {
        val dbHelper = DbHelper(this, null)
        val locations = dbHelper.getAllLocations()

        if (locations.isNotEmpty()) {
            plotLocations(locations)
        } else {
            Log.i("Database", "No locations found in the database.")
            Toast.makeText(this, "No locations available to plot.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun plotLocations(locations: List<Location>) {
        mMap.clear()
        if (locations.isNotEmpty()) {
            val polylinePoints = mutableListOf<LatLng>()

            locations.forEach { location ->
                val latLng = LatLng(location.lat, location.lng)

                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Student: ${location.studentID}")
                )
                polylinePoints.add(latLng)
            }

            mMap.addPolyline(
                com.google.android.gms.maps.model.PolylineOptions()
                    .addAll(polylinePoints)
                    .width(5f)
                    .color(android.graphics.Color.BLUE)
            )

            val firstLocation = locations.first()
            val firstLatLng = LatLng(firstLocation.lat, firstLocation.lng)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 10f))
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        // Clear the database
        val dbHelper = DbHelper(this, null)
        dbHelper.clearDatabase()

        Log.i("MainActivity", "Database wiped onDestroy")
    }
}