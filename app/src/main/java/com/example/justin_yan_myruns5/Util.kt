package com.example.justin_yan_myruns5

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException


// Referenced to XD's In Class Notes on Camera Permissions and BitMap
object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.
        decodeStream(context.
        contentResolver.
        openInputStream(imgUri))
        val matrix = Matrix()
        matrix.setRotate(getPictureRotation(imgUri).toFloat())
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    //  Flips image if taken horizontally
    //  References:
    //  https://stackoverflow.com/questions/12726860/android-how-to-detect-the-image-orientation-portrait-or-landscape-picked-fro
    //  http://www.java2s.com/example/android/graphics/get-bitmap-orientation-loaded-from-uri.html
    fun getPictureRotation(photoUri: Uri): Int {
        val image = File(photoUri.path)
        return try {
            val exif = ExifInterface(
                image.absolutePath
            )
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> ExifInterface.ORIENTATION_UNDEFINED
            }
        } catch (e: IOException) {
            Log.e("WallOfLightApp", e.message!!)
            0
        }
    }
}
