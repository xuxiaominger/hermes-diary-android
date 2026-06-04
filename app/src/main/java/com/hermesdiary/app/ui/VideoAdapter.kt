package com.hermesdiary.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.databinding.ItemVideoCardBinding
import java.text.SimpleDateFormat
import java.util.*

class VideoAdapter(
    private val onClick: (DiaryEntry) -> Unit,
    private val onLongClick: ((DiaryEntry) -> Unit)? = null
) : ListAdapter<DiaryEntry, VideoAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemVideoCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntry) {
            binding.titleText.text = entry.title.ifEmpty { "无标题视频" }
            binding.dateText.text = dateFormat.format(Date(entry.createdAt))
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
