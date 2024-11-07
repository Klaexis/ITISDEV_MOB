package com.itisdev.application

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

interface OnDataFetchedListener2 {
    fun onDataFetched(data: List<modelEvac>)
}

class getEvac (private val listener: OnDataFetchedListener2) : AsyncTask<Void, Void, List<modelEvac>>() {

    private val evacuationCollection = "evacuation"
    val firestore: FirebaseFirestore = Firebase.firestore

    override fun doInBackground(vararg params: Void?): List<modelEvac> {
        firestore.collection(evacuationCollection)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dataList = ArrayList<modelEvac>()

                for (document in querySnapshot.documents) {
                    val evacID = document.getString("evacID") ?: ""
                    val evacName = document.getString("evacName") ?: ""
                    val evacStatus = document.getString("evacStatus") ?: ""
                    val evacAddress = document.getString("evacAddress") ?: ""

                    dataList.add(
                        modelEvac(
                            evacID,
                            evacName,
                            evacStatus,
                            evacAddress
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

        override fun onPostExecute(result: List<modelEvac>) {
            listener.onDataFetched(result)
        }
}

