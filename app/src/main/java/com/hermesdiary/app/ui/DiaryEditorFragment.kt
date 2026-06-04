package com.hermesdiary.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hermesdiary.app.data.AppDatabase
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.data.EntryType
import com.hermesdiary.app.databinding.FragmentDiaryEditorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryEditorFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDiaryEditorBinding? = null
    private val binding get() = _binding!!
    private var editEntry: DiaryEntry? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiaryEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryId = arguments?.getLong("entry_id", -1) ?: -1
        if (entryId > 0) {
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(requireContext())
                val entry = withContext(Dispatchers.IO) { db.diaryDao().getById(entryId) }
                if (entry != null) {
                    editEntry = entry
                    populateFields(entry)
                }
            }
        }

        binding.btnSave.setOnClickListener { saveEntry() }
        binding.btnDelete.setOnClickListener { deleteEntry() }
    }

    private fun populateFields(entry: DiaryEntry) {
        binding.editTitle.setText(entry.title)
        binding.editContent.setText(entry.content)
        binding.btnDelete.visibility = View.VISIBLE
    }

    private fun saveEntry() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()
        if (title.isEmpty() && content.isEmpty()) {
            dismiss()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val entry = (editEntry ?: DiaryEntry(type = EntryType.DIARY)).copy(
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            withContext(Dispatchers.IO) {
                if (editEntry == null) {
                    db.diaryDao().insert(entry)
                } else {
                    db.diaryDao().update(entry)
                }
            }
            dismiss()
        }
    }

    private fun deleteEntry() {
        val entry = editEntry ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(requireContext())
                db.diaryDao().delete(entry)
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
