
package com.toxun.share.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.toxun.share.core.WifiTransferServer
import com.toxun.share.data.history.HistoryDao
import com.toxun.share.filesystem.FileSystemManager
import com.toxun.share.manager.ApkSharer
import com.toxun.share.manager.StatsManager
import com.toxun.share.manager.TextShareManager
import com.toxun.share.qr.QrManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isServerRunning by remember { mutableStateOf(false) }
    val server = remember { WifiTransferServer() }
    val fileManager = remember { FileSystemManager(context) }
    val statsManager = remember { StatsManager(context) }
    val historyDao = remember { HistoryDao(context) }
    val qrManager = remember { QrManager() }
    val scope = rememberCoroutineScope()

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        uri?.let {
            val f = fileManager.copyUriToCache(it)
            selectedFile = f
            if(f!=null){
                Toast.makeText(context, "Выбран: ${f.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title={
                Column{
                    Text("Toxun")
                    Text("Поднес. Передалось. Без интернета, без сжатия, без рекламы.", style=MaterialTheme.typography.labelSmall)
                }
            })
        },
        bottomBar = {
            NavigationBar{
                NavigationBarItem(icon={Text("↑")}, label={Text("Отправить")}, selected=tab==0, onClick={tab=0})
                NavigationBarItem(icon={Text("↓")}, label={Text("Принять")}, selected=tab==1, onClick={tab=1})
                NavigationBarItem(icon={Text("QR")}, label={Text("QR")}, selected=tab==2, onClick={tab=2})
                NavigationBarItem(icon={Text("#")}, label={Text("Статы")}, selected=tab==3, onClick={tab=3})
            }
        }
    ){ padding ->
        Box(Modifier.fillMaxSize().padding(padding)){
            when(tab){
                0 -> {
                    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                        Text("Отправить файлы", style=MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("WiFi Direct • Hotspot • Оригинал • @vixsttpn")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick={ pickLauncher.launch("*/*") }, modifier=Modifier.fillMaxWidth().height(56.dp)){ Text("ВЫБРАТЬ ФАЙЛ") }
                        if(selectedFile!=null){
                            Spacer(Modifier.height(8.dp))
                            Card(Modifier.fillMaxWidth()){
                                Column(Modifier.padding(12.dp)){
                                    Text("Файл: ${selectedFile!!.name}")
                                    Text("Размер: ${selectedFile!!.length()/1024} KB")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick={
                                server.start(selectedFile!!)
                                isServerRunning=true
                                statsManager.incSent()
                                historyDao.add(com.toxun.share.data.history.HistoryItem(selectedFile!!.name, System.currentTimeMillis(), selectedFile!!.length(), true))
                            }, modifier=Modifier.fillMaxWidth().height(56.dp), enabled=!isServerRunning){
                                Text(if(isServerRunning) "СЕРВЕР ЗАПУЩЕН" else "ЗАПУСТИТЬ ПЕРЕДАЧУ")
                            }
                            if(isServerRunning){
                                Spacer(Modifier.height(8.dp))
                                Text("Открой на другом телефоне: http://${getLocalIp(context)}:8888/")
                                Text("Или покажи QR во вкладке QR")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick={
                            ApkSharer(context).shareOwnApk()
                        }, modifier=Modifier.fillMaxWidth()){ Text("Поделиться Toxun APK") }
                        OutlinedButton(onClick={
                            TextShareManager().shareText(context, "Скачай Toxun - передача без интернета https://github.com/vixsttpn/Toxun")
                        }, modifier=Modifier.fillMaxWidth()){ Text("Поделиться ссылкой") }
                    }
                }
                1 -> {
                    var url by remember { mutableStateOf("") }
                    var downloading by remember { mutableStateOf(false) }
                    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                        Text("Принять файлы", style=MaterialTheme.typography.headlineSmall)
                        Text("NFC • QR • Авто-подключение")
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value=url, onValueChange={url=it}, label={Text("http://192.168.49.1:8888/")}, modifier=Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Button(onClick={
                            downloading=true
                            scope.launch{
                                try{
                                    val downloaded = downloadFile(context, url)
                                    if(downloaded!=null){
                                        fileManager.saveToDownloads(downloaded)
                                        statsManager.incReceived()
                                        historyDao.add(com.toxun.share.data.history.HistoryItem(downloaded.name, System.currentTimeMillis(), downloaded.length(), false))
                                        Toast.makeText(context, "Сохранено: ${downloaded.name}", Toast.LENGTH_LONG).show()
                                    }
                                }catch(e:Exception){
                                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                downloading=false
                            }
                        }, modifier=Modifier.fillMaxWidth().height(56.dp), enabled=!downloading){
                            Text(if(downloading) "СКАЧИВАЮ..." else "СКАЧАТЬ")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Или выбери файл через систему: Открой ссылку в браузере")
                    }
                }
                2 -> {
                    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                        Text("QR Transfer @vixsttpn", style=MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        val qrText = if(selectedFile!=null) "http://${getLocalIp(context)}:8888/${selectedFile!!.name}" else "http://${getLocalIp(context)}:8888/"
                        val bmp = qrManager.generateQr(qrText)
                        Card(Modifier.size(280.dp)){
                            Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){
                                if(bmp!=null){
                                    androidx.compose.foundation.Image(bitmap=androidx.compose.ui.graphics.asImageBitmap(bmp), contentDescription="QR")
                                }else{
                                    Text("QR CODE\n$qrText")
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(qrText, style=MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(12.dp))
                        Text("Сканируй чтобы подключиться без интернета")
                    }
                }
                3 -> {
                    val sent = statsManager.getTotalSent()
                    val received = statsManager.getTotalReceived()
                    val history = historyDao.getAll()
                    LazyColumn(Modifier.fillMaxSize().padding(16.dp)){
                        item{
                            Text("Toxun Stats", style=MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Card(Modifier.fillMaxWidth()){
                                Column(Modifier.padding(16.dp)){
                                    Text("Отправлено: $sent")
                                    Text("Принято: $received")
                                    Text("Сжатие: ВЫКЛ (оригинал)")
                                    Text("Реклама: НЕТ")
                                    Text("Интернет: НЕ НУЖЕН")
                                    Text("Автор: @vixsttpn")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("История:")
                        }
                        items(history){ item ->
                            ListItem(headlineContent={Text(item.fileName)}, supportingContent={Text("${if(item.isSent) "Отправлено" else "Принято"} ${java.text.SimpleDateFormat("dd.MM HH:mm").format(java.util.Date(item.timestamp))}")})
                        }
                    }
                }
            }
        }
    }
}

fun getLocalIp(context: android.content.Context): String{
    return try{
        val wifi = context.applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val ipInt = wifi.connectionInfo.ipAddress
        String.format("%d.%d.%d.%d", ipInt and 0xff, ipInt shr 8 and 0xff, ipInt shr 16 and 0xff, ipInt shr 24 and 0xff).let { if(it=="0.0.0.0") "192.168.49.1" else it }
    }catch(e:Exception){ "192.168.49.1" }
}

suspend fun downloadFile(context: android.content.Context, urlStr: String): File? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){
    try{
        val url = java.net.URL(urlStr)
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.connect()
        val fileName = urlStr.substringAfterLast("/").ifEmpty { "toxun_${System.currentTimeMillis()}" }
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { out -> conn.inputStream.copyTo(out) }
        file
    }catch(e:Exception){ null }
}
