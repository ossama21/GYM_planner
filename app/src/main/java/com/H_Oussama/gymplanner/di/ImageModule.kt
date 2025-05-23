package com.H_Oussama.gymplanner.di

import android.content.Context
import com.H_Oussama.gymplanner.util.ImageManager
import com.H_Oussama.gymplanner.util.EnhancedImageMatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageManager(@ApplicationContext context: Context): ImageManager {
        return ImageManager(context)
    }
    
    @Provides
    @Singleton
    fun provideEnhancedImageMatcher(@ApplicationContext context: Context): EnhancedImageMatcher {
        return EnhancedImageMatcher(context)
    }
} 