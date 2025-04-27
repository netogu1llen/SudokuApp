package com.sudokuapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiKey(): String {
        // En un caso real, esto debería venir de BuildConfig, un almacén seguro o un recurso protegido
        return "wLVPN1zV08lJYF7uXqgyPw==zVwp6TlVcAO1NLUf"
    }
}