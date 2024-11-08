package com.itisdev.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.itisdev.application.R.*

class homeAnnouncement : AppCompatActivity(), OnDataFetchedListener, SwipeRefreshLayout.OnRefreshListener {

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

        swipeRefreshLayout = findViewById(R.id.refreshAnnouncement)
        swipeRefreshLayout.setOnRefreshListener(this)

        // Initialize Firebase Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")

        val fetchAnnouncement = fetchAnnouncement(this)
        fetchAnnouncement.execute()

        setupFooter()
    }

    override fun onDataFetched(data: List<modelAnnouncement>) {
        adapter.updateData(data)
    }

    override fun onRefresh() {
        // Call your data fetching function here
        val fetchPost = fetchAnnouncement(this)
        fetchPost.execute()
        swipeRefreshLayout.isRefreshing = false // Reset the refresh indicator
    }
}