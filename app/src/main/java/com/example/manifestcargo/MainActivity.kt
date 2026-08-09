package com.example.manifestcargo.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// Entity untuk Tabel Cargo
@Entity(tableName = "cargo_items")
data class CargoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val awbNo: String,
    val flightNo: String,
    val pti: String,
    val pcsQty: Int,
    val pcsQtyWt: Double,
    val subTotalKg: Double,
    val description: String,
    val customer: String,
    val noPag: String
)

// Data Access Object (DAO)
@Dao
interface CargoDao {
    @Query("SELECT * FROM cargo_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<CargoItem>>

    @Query("SELECT * FROM cargo_items")
    suspend fun getAllItemsList(): List<CargoItem>

    @Insert
    suspend fun insert(cargoItem: CargoItem)

    @Query("DELETE FROM cargo_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("DELETE FROM cargo_items")
    suspend fun deleteAll()
}

// Room Database Class
@Database(entities = [CargoItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cargoDao(): CargoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cargo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

