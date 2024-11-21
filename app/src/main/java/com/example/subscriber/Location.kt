package com.example.subscriber

data class Location (
    val studentID : String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long,
    val speed : Double
)