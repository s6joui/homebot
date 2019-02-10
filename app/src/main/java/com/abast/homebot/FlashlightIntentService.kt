package com.abast.homebot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FlashlightService : Service() {

    companion object {
        const val NOTIFICATION_ID = 12345
        const val NOTIFICATION_CHANNEL_ID = "homebot_flashlight"
    }

    lateinit var cameraManager : CameraManager

    private var flashEnabled = false
    private val torchCallback = object: CameraManager.TorchCallback(){
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            flashEnabled = enabled
        }
    }

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.registerTorchCallback(torchCallback,null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,getString(R.string.flashlight_on),NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setContentTitle(getString(R.string.flashlight_on)).setContentText(getString(R.string.flashlight_on)).build()
            startForeground(NOTIFICATION_ID, notification)
        }

        cameraManager.setTorchMode(cameraManager.cameraIdList[0],!flashEnabled)
        if(flashEnabled){
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        cameraManager.unregisterTorchCallback(torchCallback)
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

}