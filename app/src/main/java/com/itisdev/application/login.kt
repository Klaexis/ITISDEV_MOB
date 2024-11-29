package com.itisdev.application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class login_act : AppCompatActivity() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val userCollection = "user"

    companion object {
        const val usernameKey: String = "nameKey"
        const val passKey: String = "passKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.login_rt)

        // Check if there is a valid user session
        val sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE)
        val residentID = sharedPreferences.getString("residentID", null)
        val typeOfUser = sharedPreferences.getString("typeOfUser", null)

        if (residentID!= null && typeOfUser!= null) {
            // Skip login page and move to home activity
            if (typeOfUser == "Resident") {
                moveToHomeRT()
            }
        } else {
            // Show login page
            setContentView(R.layout.login)
            //...
        }

        val loginRTbutton: Button = findViewById(R.id.loginRT)
        loginRTbutton.setOnClickListener {
            val username: EditText = findViewById(R.id.usernameRT)
            val userString = username.text.toString()

            if(userString.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //moveToHomeRT()
            loginUserResident(userString)
        }
    }

    fun loginUserResident(userString: String) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = "user"

        db.collection(userCollection)
            .whereEqualTo("email", userString)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.isEmpty) {

                        val document = result.documents[0]
                        val fullName = document.getString("fullName")
                        val dateOfBirth = document.getString("dateOfBirth").toString()
                        val contactNumber = document.getString("contactNumber").toString()
                        val sex = document.getString("sex").toString()

                        if (fullName != null) {

                            saveUserDetailsToPreferences(userString, fullName, dateOfBirth, contactNumber, sex)

                            Toast.makeText(this@login_act, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()

                            moveToHomeRT()
                        } else {
                            Toast.makeText(this@login_act, "User full name not found.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@login_act, "User does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Firestore Error", "Error: ${task.exception?.message}")
                    Toast.makeText(this@login_act, "Failed to check user existence.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->

                Log.e("Firestore Failure", "Error: ${exception.message}")
                Toast.makeText(this@login_act, "Error occurred: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveUserDetailsToPreferences(userString: String, fullName: String, dateOfBirth: String, contactNumber: String, sex: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("email", userString)
            .putString("fullName", fullName)
            .putString("dateOfBirth", dateOfBirth)
            .putString("contactNumber", contactNumber)
            .putString("sex", sex)
            .apply()
    }


    fun moveToHomeRT(){
        val intent = Intent(this, homeAnnouncement::class.java)
        startActivity(intent)
        finish()
    }

}