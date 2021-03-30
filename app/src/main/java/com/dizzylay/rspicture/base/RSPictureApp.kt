package com.dizzylay.rspicture.base

import android.app.Application

/**
 * @author dizzylay
 * @date 2021/3/29
 * @email liaoyanglay@outlook.com
 */
class RSPictureApp : Application() {
    companion object {
        lateinit var sApplication: Application
        const val TAG = "RSPictureApp"
    }

    override fun onCreate() {
        super.onCreate()
        sApplication = this
    }
}