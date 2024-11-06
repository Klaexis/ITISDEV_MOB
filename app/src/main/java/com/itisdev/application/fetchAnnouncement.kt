package com.itisdev.application

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

interface OnDataFetchedListener {
    fun onDataFetched(data: List<modelAnnouncement>)
}

class fetchAnnouncement(private val listener: OnDataFetchedListener) : AsyncTask<Void, Void, List<modelAnnouncement>>() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val announcementCollection = "announcements"

    override fun doInBackground(vararg params: Void?): List<modelAnnouncement> {
        firestore.collection(announcementCollection)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dataList = ArrayList<modelAnnouncement>()

                for (document in querySnapshot.documents) {
                    val announcementAuthor = document.getString("announcementAuthor") ?: ""
                    val announcementContent = document.getString("announcementContent") ?: ""
                    val announcementTitle = document.getString("announcementTitle") ?: ""
                    val announcementUploadDate = document.getString("announcementUploadDate") ?: ""

                    dataList.add(
                        modelAnnouncement(
                            announcementAuthor,
                            announcementContent,
                            announcementTitle,
                            announcementUploadDate
                        )
                    )
                }

                Log.d("DataList", "Size: ${dataList.size}")
                listener.onDataFetched(dataList)
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Failed to retrieve data: ${exception.message}")
                listener.onDataFetched(emptyList())
            }
        return ArrayList()
    }

    override fun onPostExecute(result: List<modelAnnouncement>){
        listener.onDataFetched(result)
    }

}