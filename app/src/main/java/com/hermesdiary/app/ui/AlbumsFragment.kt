package com.hermesdiary.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.hermesdiary.app.data.AppDatabase
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.data.EntryType
import com.hermesdiary.app.databinding.FragmentAlbumsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlbumAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        adapter = AlbumAdapter(
            onClick = { entry -> showAlbumDetail(entry) },
            onLongClick = { entry -> confirmDelete(entry) }
        )

        binding.albumsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.albumsRecycler.adapter = adapter

        lifecycleScope.launch {
            db.diaryDao().getEntriesByType(EntryType.ALBUM).collect { entries ->
                adapter.submitList(entries)
                binding.emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAddAlbum.setOnClickListener { createNewAlbum() }
    }

    private fun createNewAlbum() {
        lifecycleScope.launch {
            val entry = DiaryEntry(
                type = EntryType.ALBUM,
                title = "新图册 ${SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())}",
                content = ""
            )
            val id = db.diaryDao().insert(entry)
            showAlbumDetail(entry.copy(id = id))
        }
    }

    private fun showAlbumDetail(entry: DiaryEntry) {
        val images = if (entry.imageUrls.isNotEmpty()) {
            entry.imageUrls.split(",").filter { it.isNotBlank() }.size
        } else 0
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(entry.title)
            .setMessage(
                buildString {
                    append(entry.content.ifEmpty { "暂无描述" })
                    append("\n\n图片数量: $images")
                    append("\n创建于: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(entry.createdAt))}")
                }
            )
            .setPositiveButton("编辑标题") { _, _ ->
                editAlbumTitle(entry)
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun editAlbumTitle(entry: DiaryEntry) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(entry.title)
            selectAll()
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("编辑图册名称")
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
            .setTitle("删除图册")
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
