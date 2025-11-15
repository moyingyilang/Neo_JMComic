package com.yourpackage.neojmcomic.utils.persistence

import android.content.Context
import android.util.LruCache
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/** 大文件分片缓存（适配Android，基于DiskLruCache） */
class PersistenceChunk(
    context: Context,
    maxCacheSize: Long = 50 * 1024 * 1024 // 50MB默认缓存大小
) {
    private val cacheDir = File(context.cacheDir, "chunk_cache")
    private val chunkCache = LruCache<String, MutableList<ByteArray>>(100) // 内存缓存100个分片组
    private val diskCache = DiskLruCache.open(cacheDir, 1, 1, maxCacheSize)

    /** 添加分片 */
    fun push(cacheKey: String, chunk: ByteArray) {
        var chunks = chunkCache.get(cacheKey)
        if (chunks == null) {
            chunks = mutableListOf()
            chunkCache.put(cacheKey, chunks)
        }
        chunks.add(chunk)
    }

    /** 拼接分片并获取完整数据 */
    fun flush(cacheKey: String): ByteArray? {
        val chunks = chunkCache.remove(cacheKey) ?: return null
        if (chunks.isEmpty()) return ByteArray(0)
        
        // 内存拼接
        val outputStream = ByteArrayOutputStream()
        chunks.forEach { outputStream.write(it) }
        val fullData = outputStream.toByteArray()
        
        // 写入磁盘缓存
        val editor = diskCache.edit(cacheKey) ?: return fullData
        editor.newOutputStream(0).use { it.write(fullData) }
        editor.commit()
        
        return fullData
    }

    /** 从磁盘缓存读取完整数据 */
    fun getFromDisk(cacheKey: String): ByteArray? {
        val snapshot = diskCache.get(cacheKey) ?: return null
        return snapshot.getInputStream(0).readBytes().also { snapshot.close() }
    }

    /** 重置缓存（清除内存+磁盘） */
    fun reset(cacheKey: String) {
        chunkCache.remove(cacheKey)
        diskCache.remove(cacheKey)
    }

    /** 清空所有缓存 */
    fun clearAll() {
        chunkCache.evictAll()
        diskCache.delete()
    }
}
