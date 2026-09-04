
package com.toxun.share.manager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
class ApkSharer(private val context: Context){
    fun shareOwnApk(){
        try{
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            val apkFile = File(info.applicationInfo.sourceDir)
            val uri = FileProvider.getUriForFile(context, "com.toxun.share.provider", apkFile)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/vnd.android.package-archive"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Поделиться Toxun"))
        }catch(e:Exception){
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Скачай Toxun https://github.com/vixsttpn/Toxun")
            context.startActivity(Intent.createChooser(intent, "Toxun"))
        }
    }
}
