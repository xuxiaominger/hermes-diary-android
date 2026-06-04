package com.hermesdiary.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    // === 查询 ===

    @Query("SELECT * FROM entries WHERE type = :type ORDER BY createdAt DESC")
    fun getEntriesByType(type: EntryType): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): DiaryEntry?

    @Query("SELECT * FROM entries WHERE type = :type ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentByType(type: EntryType, limit: Int = 10): List<DiaryEntry>

    // === 搜索 ===

    @Query("SELECT * FROM entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<DiaryEntry>>

    // === 写入 ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // === 同步查询（非 Flow，用于导出） ===

    @Query("SELECT * FROM entries")
    suspend fun getAllEntriesSync(): List<DiaryEntry>

    @Query("SELECT * FROM entries WHERE updatedAt > :since ORDER BY updatedAt DESC")
    suspend fun getEntriesSince(since: Long): List<DiaryEntry>

    // === 统计 ===

    @Query("SELECT COUNT(*) FROM entries WHERE type = :type")
    fun countByType(type: EntryType): Flow<Int>

    @Query("SELECT COUNT(*) FROM entries")
    fun countAll(): Flow<Int>
}
