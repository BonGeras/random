package com.example.diarys22387.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.diarys22387.util.AudioPlayer
import com.example.diarys22387.util.AudioRecorder
import com.example.diarys22387.util.LocationManager
import com.example.diarys22387.service.GeofenceManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context)
    }

    @Provides
    @Singleton
    fun provideGeofenceManager(
        @ApplicationContext context: Context
    ): GeofenceManager {
        return GeofenceManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(
        @ApplicationContext context: Context
    ): AudioRecorder {
        return AudioRecorder(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer {
        return AudioPlayer(context)
    }
}
