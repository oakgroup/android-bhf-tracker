package com.active.orbit.tracker.database

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.active.orbit.tracker.database.daos.*
import com.active.orbit.tracker.retrieval.data.TripData
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData
import com.active.orbit.tracker.tracker.sensors.batteries.BatteryData
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationData
import com.active.orbit.tracker.tracker.sensors.step_counting.StepsData

@Database(
    entities = [StepsData::class, LocationData::class, ActivityData::class, TripData::class, HeartRateData::class, BatteryData::class],
    version = 7,
    exportSchema = false
)
abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun myStepDataDao(): StepsDataDAO?
    abstract fun myActivityDataDao(): ActivityDataDAO?
    abstract fun myLocationDataDao(): LocationDataDAO?
    abstract fun myHeartRateDataDao(): HeartRateDAO
    abstract fun myBatteryDAO(): BatteryDAO

    companion object {
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS TripData (" +
                            "id INTEGER NOT NULL," +
                            "startTime INTEGER NOT NULL," +
                            "endTime INTEGER NOT NULL," +
                            "activityType INTEGER NOT NULL," +
                            "radiusInMeters INTEGER NOT NULL," +
                            "steps INTEGER NOT NULL," +
                            "distanceInMeters INTEGER NOT NULL," +
                            "uploaded INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }
        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS HeartRateData (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "timeInMsecs INTEGER NOT NULL, " +
                            "heartRate INTEGER NOT NULL, " +
                            "accuracy INTEGER NOT NULL, " +
                            "timeZone INTEGER NOT NULL, " +
                            "uploaded INTEGER NOT NULL)"
                )
            }
        }
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `BatteryData` (`timeInMsecs` INTEGER NOT NULL, `batteryPercent` INTEGER NOT NULL, `isCharging` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timeZone` INTEGER NOT NULL, `uploaded` INTEGER NOT NULL)")
            }
        }


        // marking the instance as volatile to ensure atomic access to the variable
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(MyRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = androidx.room.Room.databaseBuilder(
                            context.applicationContext,
                            MyRoomDatabase::class.java, "mobility_database"
                        )
                            // how to add a migration
                            .addMigrations(MIGRATION_2_3, MIGRATION_4_5)
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(roomDatabaseCallback)
                            .build()
                    }
                }
            }
            return INSTANCE
        }

        /**
         * Override the onOpen method to populate the database.
         * For this sample, we clear the database every time it is created or opened.
         * If you want to populate the database only when the database is created for the 1st time,
         * override MyRoomDatabase.Callback()#onCreate
         */
        private val roomDatabaseCallback: Callback =
            object : Callback() {
            }
    }

    @WorkerThread
    fun logout() {
        Log.d(javaClass.name, "Clearing all the tracker database data")
        clearAllTables()
    }
}
