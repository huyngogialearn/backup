package com.passbase.passbase_sdk

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class CVibtation(context: Context) {

    private val v: Vibrator = if (Build.VERSION.SDK_INT >= 26)
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val isNewApi: Boolean = Build.VERSION.SDK_INT >= 26
    private var active: Boolean = false

    @TargetApi(Build.VERSION_CODES.O)
    private fun goVibrateNewApi(p0: Long) {

        v.vibrate(VibrationEffect.createOneShot(p0, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun goVibrateOldApi(p0: Long) {

        v.vibrate(p0)
    }

    fun vibrate(durationInMs: Long = 100) {

        if (isNewApi)
            goVibrateNewApi(durationInMs)
        else goVibrateOldApi(durationInMs)

        active = true
    }
}