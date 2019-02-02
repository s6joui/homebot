package com.abast.homebot

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.IBinder
import java.lang.Exception
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build



class FlashlightService : Service() {

    companion object {
        const val ENABLE_TORCH = "enable"
        const val DISABLE_TORCH = "disable"
    }

    override fun onBind(i: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        action?.let{
            when(it){
                ENABLE_TORCH -> setFlashlight(true)
                DISABLE_TORCH -> {
                    setFlashlight(false)
                    stopSelf()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 26) {
            val channelId = "homebot_flashlight"
            val channel = NotificationChannel(channelId,getString(R.string.flashlight_on),NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, channelId).setContentTitle(getString(R.string.flashlight_on)).setContentText(getString(R.string.flashlight_on)).build()
            startForeground(321123, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Sets flashlight state
     */
    private fun setFlashlight(enabled : Boolean){
        try {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val list = manager.cameraIdList
            manager.setTorchMode(list[0], enabled)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


}