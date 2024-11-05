package com.itisdev.application

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SOS(
    val fullName: String,
    val email: String,
    val currentAddress: String,
    val dateLastSent: String,
    val age: Int,
    val sex: String,
    val isFound: Boolean,
)

class sosController : AppCompatActivity(){
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // SOS Button
        var sosButton : ImageButton = findViewById(R.id.imageButton)

        sosButton.setOnClickListener{
            if (checkPermission()) {
                getCurrentLocation()
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        val db = Firebase.firestore

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                val currentAddress = addresses[0].getAddressLine(0)

                //get the current Session or current User
//                val sp = getSharedPreferences("userSession", MODE_PRIVATE)
//                val fullNameData = sp.getString("residentFullName", "null")
//                val residentEmail = sp.getString("residentEmail", "null")

                val fullName = "John Doe"
                val email = "john_doe@gmail.com"
                val sex = "Male"
                val isFound = false

                val dateLastSent = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                    Date()
                )

                val birthdateString = "10/25/2002"
                val birthdateCalendar = Calendar.getInstance()
                birthdateCalendar.time = SimpleDateFormat("MM/dd/yyyy").parse(birthdateString)

                val currentDateCalendar = Calendar.getInstance()

                val year1 = birthdateCalendar.get(Calendar.YEAR)
                val year2 = currentDateCalendar.get(Calendar.YEAR)
                var age = year2 - year1

                // adjust for months and days
                val month1 = birthdateCalendar.get(Calendar.MONTH)
                val month2 = currentDateCalendar.get(Calendar.MONTH)
                if (month2 < month1) {
                    age--
                } else if (month2 == month1) {
                    val day1 = birthdateCalendar.get(Calendar.DAY_OF_MONTH)
                    val day2 = currentDateCalendar.get(Calendar.DAY_OF_MONTH)
                    if (day2 < day1) {
                        age--
                    }
                }

                // Fetch all documents in the "sos" collection
                db.collection("sos")
                .get()
                .addOnSuccessListener { documents ->
                    var newId = 1 // Default ID if no documents exist
                    if (!documents.isEmpty) {
                        var maxId = 0
                        for (document in documents) {
                            val docId = document.id
                            val currentId = docId.replace("sos", "").toIntOrNull()
                            if (currentId != null && currentId > maxId) {
                                maxId = currentId
                            }
                        }
                        newId = maxId + 1
                    }

                    // Create new SOS document with the incremented ID
                    val sosData = SOS(fullName, email, currentAddress, dateLastSent, age, sex, isFound)
                    db.collection("sos").document("sos$newId")
                        .set(sosData)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                    Toast.makeText(this, "Location Sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error getting documents: ", e)
                }
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}