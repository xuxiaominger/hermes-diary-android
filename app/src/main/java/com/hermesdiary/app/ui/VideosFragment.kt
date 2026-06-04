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
import com.hermesdiary.app.databinding.FragmentVideosBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class VideosFragment : Fragment() {

    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VideoAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        adapter = VideoAdapter(
            onClick = { entry -> showVideoDetail(entry) },
            onLongClick = { entry -> confirmDelete(entry) }
        )

        binding.videosRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.videosRecycler.adapter = adapter

        lifecycleScope.launch {
            db.diaryDao().getEntriesByType(EntryType.VIDEO).collect { entries ->
                adapter.submitList(entries)
                binding.emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAddVideo.setOnClickListener { createNewVideo() }
    }

    private fun createNewVideo() {
        lifecycleScope.launch {
            val entry = DiaryEntry(
                type = EntryType.VIDEO,
                title = "新视频",
                content = ""
            )
            val id = db.diaryDao().insert(entry)
            showVideoDetail(entry.copy(id = id))
        }
    }

    private fun showVideoDetail(entry: DiaryEntry) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(entry.content)
            hint = "视频备注"
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(entry.title)
            .setMessage("视频路径: ${entry.videoUri.ifEmpty { "未设置" }}")
            .setView(input)
            .setPositiveButton("保存备注") { _, _ ->
                lifecycleScope.launch {
                    db.diaryDao().update(entry.copy(
                        content = input.text.toString().trim(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
            .setNeutralButton("编辑标题") { _, _ ->
                editVideoTitle(entry)
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun editVideoTitle(entry: DiaryEntry) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(entry.title)
            selectAll()
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("编辑视频标题")
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
            .setTitle("删除视频")
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
