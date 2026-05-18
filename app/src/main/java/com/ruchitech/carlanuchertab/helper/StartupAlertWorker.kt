package com.ruchitech.carlanuchertab.helper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StartupAlertWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return if (StartupAlertPlayer.playNow(applicationContext)) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}
