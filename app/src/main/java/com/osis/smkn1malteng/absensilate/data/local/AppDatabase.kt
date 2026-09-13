package com.osis.smkn1malteng.absensilate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StudentEntity::class, AppConfigEntity::class],
    version = 4,  // 🔥 VERSION 4 — default maxViolation kini 3 (migration v3→v4)
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun configDao(): AppConfigDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // 🔥 MIGRATION v1 → v2: Add uuid column
        // Ref: Phase 0 — NOT NULL column MUST have DEFAULT value
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add uuid column with default empty string, then populate with UUIDs
                db.execSQL("ALTER TABLE students ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")

                // Generate UUID for existing rows
                // SQLite doesn't have a built-in UUID function, so we generate in Kotlin
                // We'll handle this by updating rows after migration
                // For now, set a placeholder — will be updated on first read
                db.execSQL("UPDATE students SET uuid = 'migrated_' || id WHERE uuid = ''")
            }
        }

        // 🔥 MIGRATION v2 → v3: Add maxViolation column to app_config (single source of truth
        //    untuk ambang pelanggaran panggilan orang tua). Default 2 = perilaku lama.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_config ADD COLUMN maxViolation INTEGER NOT NULL DEFAULT 2")
            }
        }

        // 🔥 MIGRATION v3 → v4: Default max pelanggaran panggilan orang tua berubah 2 → 3.
        //    UPDATE nilai yang masih 2 (default lama) menjadi 3. Nilai selain 2 tidak disentuh.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE app_config SET maxViolation = 3 WHERE maxViolation = 2")
            }
        }

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absensi.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)  // 🔥 Add migration
                .build().also { INSTANCE = it }
            }
    }
}
