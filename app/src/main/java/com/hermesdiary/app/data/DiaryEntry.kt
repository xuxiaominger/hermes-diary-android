package com.hermesdiary.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 内容类型枚举
 */
enum class EntryType {
    DIARY,    // 日记
    ALBUM,    // 图册
    VIDEO,    // 视频
    PODCAST   // 播客
}

/**
 * 通用内容实体 — 日记、图册、视频、播客共用一张表
 */
@Entity(tableName = "entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: EntryType,           // 内容类型
    val title: String = "",        // 标题
    val content: String = "",      // 正文
    val imageUrls: String = "",    // 图片路径列表 (JSON array)
    val videoUri: String = "",     // 视频路径
    val audioUri: String = "",     // 播客音频路径
    val duration: Int = 0,         // 播客/视频时长 (秒)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
