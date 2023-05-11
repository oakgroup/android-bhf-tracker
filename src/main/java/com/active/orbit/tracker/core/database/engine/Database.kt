package com.active.orbit.tracker.core.database.engine

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.database.engine.encryption.DatabaseKeystore
import com.active.orbit.tracker.core.database.models.*
import com.active.orbit.tracker.core.database.queries.*
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Main database class
 *
 * @author omar.brugna
 */
@androidx.room.Database(entities = [DBActivity::class, DBBattery::class, DBHeartRate::class, DBLocation::class, DBStep::class, DBTrip::class], version = 2, exportSchema = false)
internal abstract class Database : RoomDatabase() {

    companion object {

        private const val encryptionEnabled = true

        @Volatile
        private var instance: Database? = null

        @Synchronized
        fun getInstance(context: Context): Database {
            if (instance == null) {
                synchronized(Database::class.java) {
                    // double check locking
                    if (instance == null) {
                        val builder = Room.databaseBuilder(context, Database::class.java, context.getString(R.string.database_name))
                        builder.fallbackToDestructiveMigration()

                        @Suppress("ConstantConditionIf")
                        if (encryptionEnabled) {
                            // encryption
                            val passphrase: ByteArray = SQLiteDatabase.getBytes(DatabaseKeystore.getSecretKey(context)?.toCharArray())
                            val factory = SupportFactory(passphrase)
                            builder.openHelperFactory(factory)
                        }

                        instance = builder.build()
                    }
                }
            }
            return instance!!
        }
    }

    fun logout() {
        backgroundThread {
            clearAllTables()
        }
    }

    abstract fun getActivities(): Activities

    abstract fun getBatteries(): Batteries

    abstract fun getHeartRates(): HeartRates

    abstract fun getLocations(): Locations

    abstract fun getSteps(): Steps

    abstract fun getTrips(): Trips
}