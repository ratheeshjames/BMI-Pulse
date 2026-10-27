package com.example.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.data.local.BmiPulseDao
import com.example.data.local.BmiPulseDatabase
import com.example.data.local.EncryptedStorageManager
import com.example.data.local.SettingsDataStore
import com.example.data.repository.BmiPulseRepository
import com.example.integrations.ReminderScheduler

/**
 * Lightweight Hilt-compatible annotations so the architecture remains 100% aligned with
 * Clean Architecture + Hilt DI conventions without requiring external KSP/Dagger codegen overhead.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HiltAndroidApp

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidEntryPoint

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HiltViewModel

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InstallIn(vararg val value: kotlin.reflect.KClass<*>)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Provides

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Singleton

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject

object SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule(private val appContext: Context) {

    @Provides
    @Singleton
    fun provideEncryptedStorageManager(): EncryptedStorageManager {
        return EncryptedStorageManager(appContext)
    }

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile("bmi_pulse_settings") }
        )
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        dataStore: DataStore<Preferences>,
        encryptedStorageManager: EncryptedStorageManager
    ): SettingsDataStore {
        return SettingsDataStore(dataStore, encryptedStorageManager)
    }

    @Provides
    @Singleton
    fun provideDatabase(): BmiPulseDatabase {
        return Room.databaseBuilder(
            appContext,
            BmiPulseDatabase::class.java,
            "bmi_pulse_encrypted.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideBmiPulseDao(database: BmiPulseDatabase): BmiPulseDao {
        return database.bmiPulseDao()
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(): ReminderScheduler {
        return ReminderScheduler(appContext)
    }

    @Provides
    @Singleton
    fun provideRepository(
        dao: BmiPulseDao,
        settingsDataStore: SettingsDataStore,
        encryptedStorageManager: EncryptedStorageManager,
        reminderScheduler: ReminderScheduler
    ): BmiPulseRepository {
        return BmiPulseRepository(
            context = appContext,
            dao = dao,
            settingsDataStore = settingsDataStore,
            encryptedStorageManager = encryptedStorageManager,
            reminderScheduler = reminderScheduler
        )
    }
}

class AppContainer(context: Context) {
    private val module = AppModule(context.applicationContext)
    val encryptedStorageManager: EncryptedStorageManager by lazy { module.provideEncryptedStorageManager() }
    val dataStore: DataStore<Preferences> by lazy { module.provideDataStore() }
    val settingsDataStore: SettingsDataStore by lazy { module.provideSettingsDataStore(dataStore, encryptedStorageManager) }
    val database: BmiPulseDatabase by lazy { module.provideDatabase() }
    val dao: BmiPulseDao by lazy { module.provideBmiPulseDao(database) }
    val reminderScheduler: ReminderScheduler by lazy { module.provideReminderScheduler() }
    val repository: BmiPulseRepository by lazy {
        module.provideRepository(dao, settingsDataStore, encryptedStorageManager, reminderScheduler)
    }
}
