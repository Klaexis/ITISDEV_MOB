package com.itisdev.application

import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.setupFooter() {
    val homebutton: ImageButton = findViewById(R.id.homebtn)
    homebutton.setOnClickListener {
        navigateTo(homeAnnouncement::class.java)
    }
    val miabutton: ImageButton = findViewById(R.id.miabtn)
    miabutton.setOnClickListener {
        navigateTo(miaController::class.java)
    }
    val evacbutton: ImageButton = findViewById(R.id.evacbtn)
    evacbutton.setOnClickListener {
        navigateTo(evacuation::class.java)
    }
    val sosbutton: ImageButton = findViewById(R.id.sosbtn)
    sosbutton.setOnClickListener {
        navigateTo(sosController::class.java)
    }
}

private fun AppCompatActivity.navigateTo(destinationActivity: Class<*>) {
    if (this::class.java != destinationActivity) {
        val intent = Intent(applicationContext, destinationActivity)
        startActivity(intent)
        finish()
    }
}