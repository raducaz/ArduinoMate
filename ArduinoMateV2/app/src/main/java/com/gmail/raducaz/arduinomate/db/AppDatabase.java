package com.gmail.raducaz.arduinomate.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.gmail.raducaz.arduinomate.AppExecutors;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.dao.ExecutionLogDao;
import com.gmail.raducaz.arduinomate.db.dao.FunctionDao;
import com.gmail.raducaz.arduinomate.db.dao.DeviceDao;
import com.gmail.raducaz.arduinomate.db.dao.FunctionExecutionDao;
import com.gmail.raducaz.arduinomate.db.dao.MockPinStateDao;
import com.gmail.raducaz.arduinomate.db.dao.PinStateDao;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;

import java.util.List;

@Database(
        entities =
                {
                        DeviceEntity.class,
                        FunctionEntity.class,
                        FunctionExecutionEntity.class,
                        ExecutionLogEntity.class,
                        PinStateEntity.class,
                        MockPinStateEntity.class
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
    public abstract FunctionExecutionDao functionExecutionDao();
    public abstract ExecutionLogDao executionLogDao();
    public abstract PinStateDao pinStateDao();
    public abstract MockPinStateDao mockPinStateDao();
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
                            addDelay();
                            // Generate the data for pre-population
                            AppDatabase database = AppDatabase.getInstance(appContext, executors);
                            List<DeviceEntity> devices = DataGenerator.generateDevices();
                            List<FunctionEntity> functions =
                                    DataGenerator.generateFunctionsForDevices(devices);

                            insertData(database, devices, functions);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                }).build();
    }
    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }
    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    private static void insertData(final AppDatabase database, final List<DeviceEntity> devices,
                                   final List<FunctionEntity> functions) {
        database.runInTransaction(() -> {
            database.deviceDao().insertAll(devices);
            database.functionDao().insertAll(functions);
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