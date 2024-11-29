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
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userCollection = "user"

        db.collection(userCollection)
            .whereEqualTo("email", userString)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.isEmpty) {

                        Toast.makeText(this@login_act, "Welcome back!", Toast.LENGTH_SHORT).show()

                        saveEmailToPreferences(userString)

                        moveToHomeRT()
                    } else {
                        Toast.makeText(this@login_act, "User does not exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle Firestore query failure
                    Log.e("Firestore Error", "Error: ${task.exception?.message}")
                    Toast.makeText(this@login_act, "Failed to check user existence.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle any additional errors
                Log.e("Firestore Failure", "Error: ${exception.message}")
                Toast.makeText(this@login_act, "Error occurred: ${exception.message}", Toast.LENGTH_SHORT).show()
            }


    }

    private fun saveEmailToPreferences(userString: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("userid", userString).apply()
    }

    fun moveToHomeRT(){
        val intent = Intent(this, homeAnnouncement::class.java)
        startActivity(intent)
        finish()
    }

}