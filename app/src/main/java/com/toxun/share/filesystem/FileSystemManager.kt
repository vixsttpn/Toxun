
package com.toxun.share.filesystem
import android.content.Context
import android.net.Uri
import java.io.File
class FileSystemManager(private val context: Context){
    fun copyUriToCache(uri: Uri): File? {
        return try{
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "toxun_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "file"}"
            val outFile = File(context.cacheDir, fileName)
            outFile.outputStream().use { out -> input.copyTo(out) }
            input.close()
            outFile
        }catch(e:Exception){ null }
    }
    fun saveToDownloads(file: File): Boolean {
        return try{
            val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val dest = File(downloads, file.name)
            file.copyTo(dest, overwrite=true)
            true
        }catch(e:Exception){ false }
    }
}
