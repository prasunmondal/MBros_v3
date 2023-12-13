package com.tech4bytes.mbrosv3.Sms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class SMSPermissions {
    companion object {

        fun askPermission(context: Context, permission: String) {
            val permissionsArray = arrayOf(permission)
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission))
                {
                    ActivityCompat.requestPermissions(context as Activity, permissionsArray, 1);
                }
                else
                {
                    ActivityCompat.requestPermissions(context as Activity, permissionsArray, 1);
                }
            }
            else
            {
                /* do nothing */
                /* permission is granted */
            }
        }

        /* And a method to override */
        fun onRequestPermissionsResult(context: Context, requestCode: Int, permissions: Array<String?>?, grantResults: IntArray) {
            when (requestCode) {
                1 -> if (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No Permission granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}