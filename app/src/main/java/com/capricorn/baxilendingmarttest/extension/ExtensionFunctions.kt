package com.capricorn.baxilendingmarttest.extension

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker

private fun Context.hasPermission(permission: String): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.checkSelfPermission(this,permission)
    } else
        PermissionChecker.checkSelfPermission(this, permission)
}


fun Context.hasWriteToStoragePermission(): Int {
    return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

fun Activity.checkWritePermission(): Boolean{
    return if(hasWriteToStoragePermission() != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 20)
        false
    }else true
}