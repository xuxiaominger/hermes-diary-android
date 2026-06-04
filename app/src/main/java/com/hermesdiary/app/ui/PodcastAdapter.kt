package com.hermesdiary.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.databinding.ItemPodcastCardBinding
import java.text.SimpleDateFormat
import java.util.*

class PodcastAdapter(
    private val onClick: (DiaryEntry) -> Unit,
    private val onLongClick: ((DiaryEntry) -> Unit)? = null
) : ListAdapter<DiaryEntry, PodcastAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPodcastCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPodcastCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntry) {
            binding.titleText.text = entry.title.ifEmpty { "无标题播客" }
            binding.contentPreview.text = entry.content
            val durationStr = if (entry.duration > 0) {
                val min = entry.duration / 60
                val sec = entry.duration % 60
                "🎙 ${min}:${sec.toString().padStart(2, '0')}"
            } else {
                "🎙 播客"
            }
            binding.dateText.text = "${dateFormat.format(Date(entry.createdAt))} · $durationStr"
            binding.root.setOnClickListener { onClick(entry) }
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(entry)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DiaryEntry, newItem: DiaryEntry) =
            oldItem == newItem
    }
}
