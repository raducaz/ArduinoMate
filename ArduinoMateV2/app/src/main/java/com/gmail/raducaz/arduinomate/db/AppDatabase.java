package com.gmail.raducaz.arduinomate.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.gmail.raducaz.arduinomate.AppExecutors;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.dao.ExecutionLogDao;
import com.gmail.raducaz.arduinomate.db.dao.FunctionDao;
import com.gmail.raducaz.arduinomate.db.dao.DeviceDao;
import com.gmail.raducaz.arduinomate.db.dao.FunctionExecutionDao;
import com.gmail.raducaz.arduinomate.db.dao.MockPinStateDao;
import com.gmail.raducaz.arduinomate.db.dao.PinStateDao;
import com.gmail.raducaz.arduinomate.db.dao.RemoteQueueDao;
import com.gmail.raducaz.arduinomate.db.dao.SettingsDao;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.RemoteQueue;
import com.gmail.raducaz.arduinomate.model.Settings;

import java.util.List;

@Database(
        entities =
                {
                        DeviceEntity.class,
                        SettingsEntity.class,
                        FunctionEntity.class,
                        FunctionExecutionEntity.class,
                        ExecutionLogEntity.class,
                        PinStateEntity.class,
                        MockPinStateEntity.class,
                        RemoteQueueEntity.class
                },
        version = 1
)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    @VisibleForTesting
    public static final String DATABASE_NAME = "arduino-mate-db";

    private static AppDatabase sInstance;
    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    //region Dao's
    public abstract DeviceDao deviceDao();
    public abstract FunctionDao functionDao();
    public abstract SettingsDao settingsDao();
    public abstract FunctionExecutionDao functionExecutionDao();
    public abstract ExecutionLogDao executionLogDao();
    public abstract PinStateDao pinStateDao();
    public abstract MockPinStateDao mockPinStateDao();
    public abstract RemoteQueueDao remoteQueueDao();
    //endregion Dao's

    //region Initialize Database
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static AppDatabase buildDatabase(final Context appContext,
                                             final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {
                            // Add a delay to simulate a long-running operation
//                            addDelay();
                            BuildDatabase(appContext, executors);
                        });
                    }
                }).build();
    }
    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */

    public static void BuildDatabase(final Context appContext,
                                     final AppExecutors executors)
    {
        // Generate the data for pre-population
        AppDatabase database = AppDatabase.getInstance(appContext, executors);
        SettingsEntity settingsEntity = DataGenerator.generateSettings();
        List<DeviceEntity> devices = DataGenerator.generateDevices();
        List<FunctionEntity> functions =
                DataGenerator.generateFunctionsForDevices(devices);

        insertData(database, devices, functions, settingsEntity);
        // notify that the database was created and it's ready to be used
        database.setDatabaseCreated();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }
    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    private static void insertData(final AppDatabase database, final List<DeviceEntity> devices,
                                   final List<FunctionEntity> functions, SettingsEntity settings) {
        database.runInTransaction(() -> {
            database.deviceDao().insertAll(devices);
            database.functionDao().insertAll(functions);
            database.settingsDao().insert(settings);
        });
    }
    //endregion Initialize Database

    private static void addDelay() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}