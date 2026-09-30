package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Purchase::class, Sale::class, Customer::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create customers table
                database.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `address` TEXT, `notes` TEXT)")
                
                // Add columns to purchases
                database.execSQL("ALTER TABLE `purchases` ADD COLUMN `trackingNumber` TEXT")
                database.execSQL("ALTER TABLE `purchases` ADD COLUMN `shippingAgency` TEXT")
                
                // Add columns to sales
                database.execSQL("ALTER TABLE `sales` ADD COLUMN `customerId` INTEGER")
                database.execSQL("ALTER TABLE `sales` ADD COLUMN `totalAmountUsd` REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE `sales` ADD COLUMN `amountPaidUsd` REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "importmanager_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
