package com.pdfmaster.app.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val lastOpened: Long = System.currentTimeMillis(),
    val toolUsed: String? = null
)

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT 50")
    fun getAllRecentFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT :limit")
    fun getRecentFiles(limit: Int): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM recent_files")
    suspend fun count(): Int

    @Query("UPDATE recent_files SET lastOpened = :time WHERE path = :path")
    suspend fun updateLastOpened(path: String, time: Long = System.currentTimeMillis())
}

@Database(entities = [RecentFileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_master_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
