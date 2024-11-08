package com.itisdev.application

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Missing(
    val missingFullName : String,
    val age : Int,
    val sex : String,
    val timeLastSeen : String,
    val areaLastSeen : String,
    val description : String,
    val contactNum : String,
    val dateSubmitted : String,
    val filedBy : String,
    val isFound : Boolean,
)

class miaController : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var fullNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var areaLastSeenEditText: EditText
    private lateinit var timeLastSeenEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var sexRadioGroup: RadioGroup
    private lateinit var maleRadioButton: RadioButton
    private lateinit var femaleRadioButton: RadioButton
    private lateinit var submitButton: Button

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mia)

        swipeRefreshLayout = findViewById(R.id.refreshFileMissingPerson)
        swipeRefreshLayout.setOnRefreshListener(this)

        fileMissingPerson()

        setupFooter() // Call the footer setup function
    }

    private fun fileMissingPerson() {
        val db = Firebase.firestore

        fullNameEditText = findViewById(R.id.missingName)
        ageEditText = findViewById(R.id.missingAge)
        sexRadioGroup = findViewById(R.id.missingSex)
        maleRadioButton = findViewById(R.id.missingMale)
        femaleRadioButton = findViewById(R.id.missingFemale)
        timeLastSeenEditText = findViewById(R.id.missingTime)
        areaLastSeenEditText = findViewById(R.id.missingLocation)
        descriptionEditText = findViewById(R.id.missingDescription)

        val editTexts = listOf(fullNameEditText, descriptionEditText, areaLastSeenEditText, timeLastSeenEditText, ageEditText)

        submitButton = findViewById(R.id.submitbtn)

        submitButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val areaLastSeen = areaLastSeenEditText.text.toString()
            val timeLastSeen = timeLastSeenEditText.text.toString()
            val age = ageEditText.text.toString().toIntOrNull()
            val sex: String

            //get the current Session or current User
//            val sp = getSharedPreferences("userSession", MODE_PRIVATE)
//            val fullNameData = sp.getString("residentFullName", "null")
//            val residentContactNumber = sp.getString("residentContactNumber", "null")

            val dateSubmitted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                Date()
            )
            val filedBy = "John Doe"
            val contactNum = "09123456789"
            val isFound = false

            if (fullName.isEmpty() || description.isEmpty() || areaLastSeen.isEmpty() || timeLastSeen.isEmpty() || age == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (sexRadioGroup.checkedRadioButtonId) {
                R.id.missingMale -> sex = "Male"
                R.id.missingFemale -> sex = "Female"
                else -> {
                    Toast.makeText(this, "Please select a sex", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Fetch all documents in the "sos" collection
            db.collection("mia")
            .get()
            .addOnSuccessListener { documents ->
                var newId = 1 // Default ID if no documents exist
                if (!documents.isEmpty) {
                    var maxId = 0
                    for (document in documents) {
                        val docId = document.id
                        val currentId = docId.replace("mia", "").toIntOrNull()
                        if (currentId != null && currentId > maxId) {
                            maxId = currentId
                        }
                    }
                    newId = maxId + 1
                }

                // Create new SOS document with the incremented ID
                val missingData = Missing(fullName, age, sex, timeLastSeen, areaLastSeen, description, contactNum, dateSubmitted, filedBy, isFound)

                db.collection("mia").document("mia$newId")
                    .set(missingData)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                Toast.makeText(this, "Missing Person Details Sent", Toast.LENGTH_SHORT).show()
                editTexts.forEach { it.text.clear() }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
        }
    }

    override fun onRefresh() {
        fullNameEditText.text.clear()
        descriptionEditText.text.clear()
        areaLastSeenEditText.text.clear()
        timeLastSeenEditText.text.clear()
        ageEditText.text.clear()

        maleRadioButton.isChecked = false
        femaleRadioButton.isChecked = false

        swipeRefreshLayout.isRefreshing = false // Reset the refresh indicator
    }
}