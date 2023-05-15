package com.active.orbit.tracker.core.database.engine

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.database.engine.encryption.TrackerDatabaseKeystore
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
@androidx.room.Database(entities = [TrackerDBActivity::class, TrackerDBBattery::class, TrackerDBHeartRate::class, TrackerDBLocation::class, TrackerDBStep::class, TrackerDBTrip::class], version = 1, exportSchema = false)
internal abstract class TrackerDatabase : RoomDatabase() {

    companion object {

        private const val encryptionEnabled = true

        @Volatile
        private var instance: TrackerDatabase? = null

        @Synchronized
        fun getInstance(context: Context): TrackerDatabase {
            if (instance == null) {
                synchronized(TrackerDatabase::class.java) {
                    // double check locking
                    if (instance == null) {
                        val builder = Room.databaseBuilder(context, TrackerDatabase::class.java, context.getString(R.string.tracker_database_name))
                        builder.fallbackToDestructiveMigration()

                        @Suppress("ConstantConditionIf")
                        if (encryptionEnabled) {
                            // encryption
                            val passphrase: ByteArray = SQLiteDatabase.getBytes(TrackerDatabaseKeystore.getSecretKey(context)?.toCharArray())
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

    abstract fun getActivities(): TrackerActivities

    abstract fun getBatteries(): TrackerBatteries

    abstract fun getHeartRates(): TrackerHeartRates

    abstract fun getLocations(): TrackerLocations

    abstract fun getSteps(): TrackerSteps

    abstract fun getTrips(): TrackerTrips
}