package com.ruchitech.carlanuchertab.helper

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StartupAlertWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.i("StartupAlertWorker", "doWork: Worker started execution.")
        return if (StartupAlertPlayer.playNow(applicationContext)) {
            Log.i("StartupAlertWorker", "doWork: Worker completed successfully. Alert audio played.")
            Result.success()
        } else {
            Log.e("StartupAlertWorker", "doWork: Worker failed. Alert audio failed to play.")
            Result.failure()
        }
    }
}
