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
import com.hermesdiary.app.databinding.FragmentDiaryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DiaryAdapter
    private lateinit var db: AppDatabase
    private var entryIdToEdit: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        adapter = DiaryAdapter(
            onClick = { entry -> openEditor(entry.id) },
            onLongClick = { entry -> confirmDelete(entry) }
        )

        binding.diaryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.diaryRecycler.adapter = adapter

        // 观察数据 (Flow → collect)
        lifecycleScope.launch {
            db.diaryDao().getEntriesByType(EntryType.DIARY).collect { entries ->
                adapter.submitList(entries)
                binding.emptyView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // FAB 添加
        binding.fabAddDiary.setOnClickListener { openEditor(null) }
    }

    private fun openEditor(entryId: Long?) {
        entryIdToEdit = entryId
        val editor = DiaryEditorFragment()
        val args = Bundle()
        if (entryId != null) args.putLong("entry_id", entryId)
        editor.arguments = args
        editor.show(parentFragmentManager, "DiaryEditor")
    }

    private fun confirmDelete(entry: DiaryEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("删除日记")
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
