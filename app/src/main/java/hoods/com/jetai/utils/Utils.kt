package hoods.com.jetai.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Patterns
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun isValidEmail(email: String): Boolean {
    if (email.isEmpty()) return false
    val emailPattern = Patterns.EMAIL_ADDRESS
    return emailPattern.matcher(email).matches()
}

fun formatDate(date: Date): String {
    val pattern = "yyyy-MM-dd HH:mm:ss"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(date)
}

fun saveImageToFileAndGetUri(bitmap: Bitmap, context: Context): Uri? {
    val fileName = "${System.currentTimeMillis()}.jpg"
    val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(imagesDir, fileName)
    var fos: OutputStream? = null
    try {
        fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        return Uri.fromFile(imageFile)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fos?.close()
    }
    return null
}