package com.growgardentracker.android.util

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorageHelper {
    fun copyUriToInternalStorage(context: Context, uri: Uri): String {
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()
        val file = File(imageDir, "grow_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}
