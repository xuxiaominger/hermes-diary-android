package com.hermesdiary.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hermesdiary.app.data.AppDatabase
import com.hermesdiary.app.data.DataExporter
import com.hermesdiary.app.data.EntryType
import com.hermesdiary.app.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())

        // 加载统计数据 (Flow → collect)
        lifecycleScope.launch {
            db.diaryDao().countByType(EntryType.DIARY).collect { count ->
                binding.statDiary.text = count.toString()
            }
        }
        lifecycleScope.launch {
            db.diaryDao().countByType(EntryType.ALBUM).collect { count ->
                binding.statAlbums.text = count.toString()
            }
        }
        lifecycleScope.launch {
            db.diaryDao().countByType(EntryType.VIDEO).collect { count ->
                binding.statVideos.text = count.toString()
            }
        }
        lifecycleScope.launch {
            db.diaryDao().countByType(EntryType.PODCAST).collect { count ->
                binding.statPodcast.text = count.toString()
            }
        }

        // 导出到 Hermes
        binding.cardExport.setOnClickListener { exportToHermes() }

        // 关于
        binding.cardAbout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Hermes Diary v2.0.0")
                .setMessage("许笑铭的个人博客客户端\n\n所有数据存储在本地设备上。\n通过「同步到网站」功能，可以将文字内容导出并交给 Hermes 同步到网页端。\n\n技术栈: Kotlin + Room + Material Design")
                .setPositiveButton("好的", null)
                .show()
        }
    }

    private fun exportToHermes() {
        lifecycleScope.launch {
            try {
                val json = DataExporter.exportToJson(requireContext())
                val file = DataExporter.exportToFile(requireContext())

                // 弹窗显示导出成功 + 分享
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("✅ 导出成功")
                    .setMessage(
                        "数据已导出为 JSON 文件\n\n" +
                        "文件位置:\n${file.absolutePath}\n\n" +
                        "请将文件通过 Telegram 发送给 @herme2026_bot\n" +
                        "Hermes 会自动处理并同步到网页端。"
                    )
                    .setPositiveButton("分享文件") { _, _ ->
                        shareFile(file)
                    }
                    .setNegativeButton("复制 JSON") { _, _ ->
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Hermes Diary Export", json))
                        android.widget.Toast.makeText(requireContext(), "JSON 已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("取消", null)
                    .show()
            } catch (e: Exception) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("导出失败")
                    .setMessage(e.message ?: "未知错误")
                    .setPositiveButton("确定", null)
                    .show()
            }
        }
    }

    private fun shareFile(file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "分享到 Telegram"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
