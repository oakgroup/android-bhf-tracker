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
import uk.ac.shef.tracker.core.database.models.*
import uk.ac.shef.tracker.core.database.queries.*
import uk.ac.shef.tracker.core.utils.background
import kotlin.coroutines.CoroutineContext

/**
 * Main database class
 *
 * @author omar.brugna
 */
@androidx.room.Database(entities = [TrackerDBActivity::class, TrackerDBBattery::class, TrackerDBHeartRate::class, TrackerDBLocation::class, TrackerDBStep::class, TrackerDBTrip::class], version = 1, exportSchema = false)
internal abstract class TrackerDatabase : RoomDatabase(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

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