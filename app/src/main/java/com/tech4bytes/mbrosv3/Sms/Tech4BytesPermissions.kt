package com.tech4bytes.mbrosv3.Sms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat


class Tech4BytesPermissions {
    companion object {

        fun requestSMSPermission(context: Context, activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                    Log.d("permission", "permission denied to SEND_SMS - requesting it")
                    val permissions = arrayOf(android.Manifest.permission.SEND_SMS)
                    ActivityCompat.requestPermissions(activity, permissions, 123)
                }
            }
        }
    }
}