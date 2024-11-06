package com.itisdev.application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnnouncementAdapter(private var data: MutableList<modelAnnouncement>, homeactivity: homeAnnouncement) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementAdapter.AnnouncementViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.announcementdata, parent, false)

        return AnnouncementAdapter.AnnouncementViewHolder(view)

    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {

        val modelAnnouncement = data.getOrNull(position)
        modelAnnouncement?.let { holder.bindData(it) }
    }

    class AnnouncementViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        val announcementAuthor: TextView = itemView.findViewById(R.id.username)
        val announcementContent: TextView = itemView.findViewById(R.id.caption_post)
        val announcementTitle: TextView = itemView.findViewById(R.id.title)
        val announcementUploadDate: TextView = itemView.findViewById(R.id.date)


        fun bindData(data: modelAnnouncement) {
            announcementAuthor.text = data.announcementAuthor
            announcementContent.text = data.announcementContent
            announcementTitle.text = data.announcementTitle
            announcementUploadDate.text = data.announcementUploadDate
        }

    }

    fun updateData(newData: List<modelAnnouncement>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }
}