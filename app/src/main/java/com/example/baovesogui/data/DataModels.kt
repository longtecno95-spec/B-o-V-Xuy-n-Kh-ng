package com.example.baovesogui.data

import android.content.Context
import androidx.room.*

enum class VehicleType { MOTORBIKE, CAR }

@Entity(tableName = "danh_sach_xe")
data class XeInPark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bienSo: String,
    val soPhong: String,
    val loaiXe: VehicleType = VehicleType.MOTORBIKE,
    val thoiGianVao: Long = System.currentTimeMillis(),
    var trangThai: Int = 1 // 1: Trong bãi, 0: Đã ra
)

@Dao
interface XeDao {
    @Insert
    suspend fun insertXe(xe: XeInPark): Long

    @Query("SELECT * FROM danh_sach_xe WHERE soPhong = :roomNum AND trangThai = 1 ORDER BY thoiGianVao DESC")
    suspend fun getActiveByRoom(roomNum: String): List<XeInPark>

    @Query("UPDATE danh_sach_xe SET trangThai = 0 WHERE id = :xeId")
    suspend fun checkoutXe(xeId: Long)

    @Query("SELECT * FROM danh_sach_xe WHERE trangThai = 1")
    suspend fun getAllActive(): List<XeInPark>
}

@Database(entities = [XeInPark::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun xeDao(): XeDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "bao_ve_so.db"
                ).build().also { instance = it }
            }
    }
}
