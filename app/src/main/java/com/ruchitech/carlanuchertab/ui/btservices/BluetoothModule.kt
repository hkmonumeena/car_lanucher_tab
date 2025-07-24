package com.ruchitech.carlanuchertab.ui.btservices

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothConnectionManager(): BluetoothConnectionManager {
        return BluetoothConnectionManager()
    }
}
