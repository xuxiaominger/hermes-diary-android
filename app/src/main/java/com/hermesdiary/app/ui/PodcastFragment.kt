package com.hermesdiary.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hermesdiary.app.data.AppDatabase
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.data.EntryType
import com.hermesdiary.app.databinding.FragmentPodcastBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PodcastFragment : Fragment() {

    private var _binding: FragmentPodcastBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PodcastAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPodcastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        adapter = PodcastAdapter(
            onClick = { entry -> showPodcastDetail(entry) },
            onLongClick = { entry -> confirmDelete(entry) }
        )

        binding.podcastRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.podcastRecycler.adapter = adapter

        lifecycleScope.launch {
            db.diaryDao().getEntriesByType(EntryType.PODCAST).collect { entries ->
                adapter.submitList(entries)
                binding.emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAddPodcast.setOnClickListener { createNewPodcast() }
    }

    private fun createNewPodcast() {
        lifecycleScope.launch {
            val entry = DiaryEntry(
                type = EntryType.PODCAST,
                title = "新播客 ${SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date())}",
                content = ""
            )
            val id = db.diaryDao().insert(entry)
            showPodcastDetail(entry.copy(id = id))
        }
    }

    private fun showPodcastDetail(entry: DiaryEntry) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(entry.content)
            hint = "播客文字稿/描述"
            gravity = android.view.Gravity.TOP
            minLines = 5
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 ${entry.title}")
            .setMessage("音频路径: ${entry.audioUri.ifEmpty { "未录制" }}\n时长: ${entry.duration}秒")
            .setView(input)
            .setPositiveButton("保存文字") { _, _ ->
                lifecycleScope.launch {
                    db.diaryDao().update(entry.copy(
                        content = input.text.toString().trim(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
            .setNeutralButton("编辑标题") { _, _ ->
                editPodcastTitle(entry)
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun editPodcastTitle(entry: DiaryEntry) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(entry.title)
            selectAll()
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("编辑播客标题")
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                lifecycleScope.launch {
                    db.diaryDao().update(entry.copy(title = input.text.toString().trim(), updatedAt = System.currentTimeMillis()))
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmDelete(entry: DiaryEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("删除播客")
            .setMessage("确定删除「${entry.title}」吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch { db.diaryDao().delete(entry) }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
