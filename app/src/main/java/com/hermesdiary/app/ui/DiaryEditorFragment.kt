package com.hermesdiary.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hermesdiary.app.data.AppDatabase
import com.hermesdiary.app.data.DiaryEntry
import com.hermesdiary.app.data.EntryType
import com.hermesdiary.app.databinding.FragmentDiaryEditorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiaryEditorFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDiaryEditorBinding? = null
    private val binding get() = _binding!!
    private var editEntry: DiaryEntry? = null
    var onSaved: (() -> Boolean)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiaryEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryId = arguments?.getLong("entry_id", -1) ?: -1
        if (entryId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(requireContext())
                val entry = db.diaryDao().getById(entryId)
                if (entry != null) {
                    editEntry = entry
                    requireActivity().runOnUiThread { populateFields(entry) }
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

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(requireContext())
            val entry = (editEntry ?: DiaryEntry(type = EntryType.DIARY)).copy(
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            if (editEntry == null) {
                db.diaryDao().insert(entry)
            } else {
                db.diaryDao().update(entry)
            }
            requireActivity().runOnUiThread { dismiss() }
        }
    }

    private fun deleteEntry() {
        val entry = editEntry ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(requireContext())
            db.diaryDao().delete(entry)
            requireActivity().runOnUiThread { dismiss() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
