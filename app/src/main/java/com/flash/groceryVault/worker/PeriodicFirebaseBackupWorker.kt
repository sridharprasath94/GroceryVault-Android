package com.flash.groceryVault.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flash.groceryVault.di.AppContainer

class PeriodicFirebaseBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val container = AppContainer(applicationContext)
            container.firebaseBackupServiceForCurrentUser().backupNow()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
