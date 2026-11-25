package com.example.edu_prac_3

import android.app.Application
import com.example.edu_prac_3.data.GradeBookRepository
import com.example.edu_prac_3.data.local.AppDatabase
import com.example.edu_prac_3.data.local.DemoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Класс приложения: держит единственные экземпляры базы и репозитория
 * и один раз при первом запуске наполняет пустую базу демонстрационными данными.
 */
class GradeBookApp : Application() {

    /** Скоуп уровня приложения для операций, не привязанных к экрану. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.get(this) }

    val repository: GradeBookRepository by lazy { GradeBookRepository(database) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            DemoData.seedIfEmpty(database)
        }
    }
}
