package com.osis.smkn1malteng.absensilate.sync

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.osis.smkn1malteng.absensilate.data.local.StudentEntity
import com.osis.smkn1malteng.absensilate.data.model.StudentClass
import com.osis.smkn1malteng.absensilate.data.model.StudentMajor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * CompactSerializer — Handles GZIP compression + Base64URL encoding for efficient
 * data transfer over QR handshake.
 *
 * Flow: List<StudentEntity> → JSON → GZIP compress → Base64URL
 *
 * Performance: 60-80% size reduction, compression < 10ms for typical payloads.
 * Ref: Phase 0 verification — GZIP compression on JSON gives optimal results.
 */
object CompactSerializer {

    private val gson = Gson()

    /**
     * Data Transfer Object for serialization — minimal fields only.
     * UUID is used as the primary key for merge operations.
     */
    private data class StudentCompact(
        val uuid: String,
        val name: String,
        val phone: String,
        val parentPhone: String?,
        val kelas: String,          // display name, e.g., "X"
        val jurusan: String,        // display name, e.g., "TJKT 1"
        val violationCount: Int,
        val timestamps: List<Long>
    )

    /**
     * Serialize and compress a list of students to a URL-safe Base64 string.
     *
     * @param students List of StudentEntity to serialize
     * @return URL-safe Base64 encoded GZIP compressed JSON
     */
    fun serialize(students: List<StudentEntity>): String {
        // 1. Convert to compact DTO
        val compactList = students.map { student ->
            StudentCompact(
                uuid = student.uuid,
                name = student.name,
                phone = student.phone,
                parentPhone = student.parentPhone,
                kelas = student.kelas.display,
                jurusan = student.jurusan.display,
                violationCount = student.violationCount,
                timestamps = student.timestamps
            )
        }

        // 2. Convert to JSON
        val json = gson.toJson(compactList)

        // 3. GZIP compress
        val compressed = gzipCompress(json)

        // 4. Base64 URL-safe encode (NO_WRAP = no line breaks, URL_SAFE = URL-safe)
        return Base64.encodeToString(compressed, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /**
     * Deserialize and decompress a URL-safe Base64 string back to List<StudentEntity>.
     *
     * @param encoded URL-safe Base64 encoded GZIP compressed JSON
     * @return List of StudentEntity, or empty list on error
     */
    fun deserialize(encoded: String): List<StudentEntity> {
        return try {
            // 1. Base64 URL-safe decode
            val compressed = Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP)

            // 2. GZIP decompress
            val json = gzipDecompress(compressed)

            // 3. Parse JSON
            val type = object : TypeToken<List<StudentCompact>>() {}.type
            val compactList: List<StudentCompact> = gson.fromJson(json, type)

            // 4. Convert back to StudentEntity
            compactList.mapNotNull { compact ->
                try {
                    val kelas = StudentClass.values().firstOrNull { it.display == compact.kelas }
                        ?: StudentClass.X
                    val jurusan = StudentMajor.values().firstOrNull { it.display == compact.jurusan }
                        ?: StudentMajor.TJKT_1

                    StudentEntity(
                        uuid = compact.uuid.ifEmpty { java.util.UUID.randomUUID().toString() },
                        name = compact.name,
                        phone = compact.phone,
                        parentPhone = compact.parentPhone,
                        kelas = kelas,
                        jurusan = jurusan,
                        violationCount = compact.violationCount,
                        timestamps = compact.timestamps
                    )
                } catch (e: Exception) {
                    null // Skip individual student if mapping fails
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * GZIP compress a string to byte array.
     * Compression ratio: typically 60-80% size reduction.
     * Ref: Phase 0 — GZIP compression < 10ms for most cases.
     */
    private fun gzipCompress(input: String): ByteArray {
        return ByteArrayOutputStream().use { baos ->
            GZIPOutputStream(baos).use { gzip ->
                gzip.write(input.toByteArray(Charsets.UTF_8))
            }
            baos.toByteArray()
        }
    }

    /**
     * GZIP decompress a byte array back to string.
     */
    private fun gzipDecompress(input: ByteArray): String {
        return ByteArrayInputStream(input).use { bais ->
            GZIPInputStream(bais).use { gzip ->
                gzip.readBytes().toString(Charsets.UTF_8)
            }
        }
    }

    /**
     * Check if a string is a legacy Base64 QR code (from old QrSyncEngine).
     * Legacy QR codes are Base64 encoded JSON without GZIP compression.
     */
    fun isLegacyFormat(encoded: String): Boolean {
        return try {
            // Try to decode as legacy format (plain Base64 JSON)
            val decoded = Base64.decode(encoded, Base64.NO_WRAP)
            val json = String(decoded, Charsets.UTF_8)
            // Check if it looks like JSON (starts with '[' or '{')
            json.trim().startsWith("[") || json.trim().startsWith("{")
        } catch (e: Exception) {
            false
        }
    }
}

