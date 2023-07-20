/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.engine

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.database.engine.encryption.TrackerDatabaseKeystore
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.database.models.TrackerDBBattery
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.models.TrackerDBStep
import uk.ac.shef.tracker.core.database.models.TrackerDBTrip
import uk.ac.shef.tracker.core.database.queries.TrackerActivities
import uk.ac.shef.tracker.core.database.queries.TrackerBatteries
import uk.ac.shef.tracker.core.database.queries.TrackerHeartRates
import uk.ac.shef.tracker.core.database.queries.TrackerLocations
import uk.ac.shef.tracker.core.database.queries.TrackerSteps
import uk.ac.shef.tracker.core.database.queries.TrackerTrips
import uk.ac.shef.tracker.core.utils.background
import kotlin.coroutines.CoroutineContext

/**
 * Main [RoomDatabase] class
 *
 * @return the public key encrypted
 */
@androidx.room.Database(entities = [TrackerDBActivity::class, TrackerDBBattery::class, TrackerDBHeartRate::class, TrackerDBLocation::class, TrackerDBStep::class, TrackerDBTrip::class], version = 1, exportSchema = false)
internal abstract class TrackerDatabase : RoomDatabase(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    companion object {

        /**
         * The database will be encrypted if this variable is set to true
         */
        private const val encryptionEnabled = true

        @Volatile
        private var instance: TrackerDatabase? = null

        /**
         * Get the singleton instance of the [TrackerDatabase]
         *
         * @param context an instance of the [Context]
         * @return the singleton instance of the [TrackerDatabase]
         */
        @Synchronized
        fun getInstance(context: Context): TrackerDatabase {
            if (instance == null) {
                synchronized(TrackerDatabase::class.java) {
                    // double check locking
                    if (instance == null) {
                        val builder = Room.databaseBuilder(context, TrackerDatabase::class.java, context.getString(R.string.tracker_database_name))
                        builder.fallbackToDestructiveMigration()

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

    /**
     * Clear all the database tables asynchronously
     */
    fun logout() {
        background {
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