package com.hermesdiary.app.data

import android.content.Context
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 数据导出工具 — 把本地数据导出为 JSON，用于同步到网页端
 */
object DataExporter {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    /**
     * 导出数据为 JSON 字符串
     */
    suspend fun exportToJson(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val entries = db.diaryDao().getAllEntriesSync()
        val data = ExportData(
            appVersion = "2.0.0",
            exportDate = System.currentTimeMillis(),
            entries = entries
        )
        return gson.toJson(data)
    }

    /**
     * 导出到文件，返回文件路径
     */
    suspend fun exportToFile(context: Context): File {
        val json = exportToJson(context)
        val fileName = "hermes_diary_backup_${dateFormat.format(Date())}.json"
        val dir = File(context.getExternalFilesDir(null), "hermes_exports")
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeText(json)
        return file
    }

    /**
     * 仅导出新增/更新的内容（自上一同步时间之后）
     */
    suspend fun exportNewContent(context: Context, sinceTimestamp: Long): ExportData {
        val db = AppDatabase.getInstance(context)
        val entries = db.diaryDao().getEntriesSince(sinceTimestamp)
        return ExportData(
            appVersion = "2.0.0",
            exportDate = System.currentTimeMillis(),
            entries = entries
        )
    }

    data class ExportData(
        val appVersion: String,
        val exportDate: Long,
        val entries: List<DiaryEntry>
    )
}

// All sync methods are now directly in DiaryDao
