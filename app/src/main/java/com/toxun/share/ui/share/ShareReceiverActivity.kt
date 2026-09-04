
package com.toxun.share.ui.share
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.toxun.share.core.WifiTransferServer
import com.toxun.share.filesystem.FileSystemManager
import com.toxun.share.manager.StatsManager
import com.toxun.share.data.history.HistoryDao
import com.toxun.share.qr.QrManager
import java.io.File

class ShareReceiverActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        val text: String? = intent.getStringExtra(Intent.EXTRA_TEXT)
        setContent {
            var status by remember { mutableStateOf("Подготовка...") }
            val context = LocalContext.current
            val fileManager = remember { FileSystemManager(context) }
            val qrManager = remember { QrManager() }
            var file: File? by remember { mutableStateOf(null) }
            var ip by remember { mutableStateOf("192.168.49.1") }
            LaunchedEffect(Unit){
                try{
                    if(uri!=null){
                        file = fileManager.copyUriToCache(uri)
                        status = "Файл: ${file?.name} ${file?.length()?.div(1024)} KB"
                        // get local IP
                        val wifi = context.applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                        val ipInt = wifi.connectionInfo.ipAddress
                        ip = String.format("%d.%d.%d.%d", ipInt and 0xff, ipInt shr 8 and 0xff, ipInt shr 16 and 0xff, ipInt shr 24 and 0xff)
                        if(ip=="0.0.0.0") ip="192.168.43.1"
                        val server = WifiTransferServer()
                        file?.let { server.start(it) }
                        status = "Сервер запущен: http://$ip:8888/ - покажи QR получателю"
                    }else if(text!=null){
                        status = "Текст: $text"
                    }
                }catch(e:Exception){ status="Ошибка: ${e.message}" }
            }
            Column(Modifier.fillMaxSize().padding(16.dp)){
                Text("Toxun - Отправка", style=MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(status)
                Spacer(Modifier.height(16.dp))
                Button(onClick={ 
                    val stats = StatsManager(context)
                    stats.incSent()
                    val history = HistoryDao(context)
                    file?.let { history.add(com.toxun.share.data.history.HistoryItem(it.name, System.currentTimeMillis(), it.length(), true)) }
                    finish()
                }, modifier=Modifier.fillMaxWidth()){ Text("Готово") }
            }
        }
    }
}
