package com.itisdev.application

import android.Manifest
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.itisdev.application.R.*

data class User(val name: String? = null, val age: Int? = null)

class homeAnnouncement : AppCompatActivity(), OnDataFetchedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnnouncementAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layout.home)

        recyclerView = findViewById(id.rvAnnouncement)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        val fetchAnnouncement = fetchAnnouncement(this)
        fetchAnnouncement.execute()

        setupFooter()
    }

    override fun onDataFetched(data: List<modelAnnouncement>) {
        adapter.updateData(data)
    }
}