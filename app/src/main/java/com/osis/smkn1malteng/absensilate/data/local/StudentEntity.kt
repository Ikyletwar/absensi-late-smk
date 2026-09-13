package com.osis.smkn1malteng.absensilate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.osis.smkn1malteng.absensilate.data.model.StudentClass
import com.osis.smkn1malteng.absensilate.data.model.StudentMajor
import java.util.UUID

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),  // 🔥 NEW: Unique identifier for sync
    val name: String,
    val phone: String,
    val parentPhone: String? = null,
    val kelas: StudentClass,
    val jurusan: StudentMajor,
    var violationCount: Int = 0,
    val timestamps: List<Long> = emptyList()
)
