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
import com.toxun.share.data.history.HistoryItem
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
        uri?.let { val f = fileManager.copyUriToCache(it); selectedFile = f }
    }
    Scaffold(
        topBar = { TopAppBar(title={ Column{ Text("Toxun"); Text("Поднес. Передалось. Без интернета.", style=MaterialTheme.typography.labelSmall) } }) },
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
                0 -> Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                    Text("Отправить файлы", style=MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick={ pickLauncher.launch("*/*") }, modifier=Modifier.fillMaxWidth().height(56.dp)){ Text("ВЫБРАТЬ ФАЙЛ") }
                    if(selectedFile!=null){
                        Spacer(Modifier.height(8.dp))
                        Text("Файл: ${selectedFile!!.name}")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick={
                            server.start(selectedFile!!)
                            isServerRunning=true
                            statsManager.incSent()
                            historyDao.add(HistoryItem(selectedFile!!.name, System.currentTimeMillis(), selectedFile!!.length(), true))
                        }, modifier=Modifier.fillMaxWidth(), enabled=!isServerRunning){ Text(if(isServerRunning) "СЕРВЕР ЗАПУЩЕН" else "ЗАПУСТИТЬ") }
                        if(isServerRunning){ Text("http://${getLocalIp(context)}:8888/") }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick={ ApkSharer(context).shareOwnApk() }, modifier=Modifier.fillMaxWidth()){ Text("Поделиться APK") }
                }
                1 -> {
                    var url by remember { mutableStateOf("") }
                    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                        Text("Принять файлы")
                        OutlinedTextField(value=url, onValueChange={url=it}, label={Text("http://192.168.49.1:8888/")}, modifier=Modifier.fillMaxWidth())
                        Button(onClick={
                            scope.launch{
                                val f = downloadFile(context, url)
                                if(f!=null){ statsManager.incReceived(); Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show() }
                            }
                        }, modifier=Modifier.fillMaxWidth()){ Text("СКАЧАТЬ") }
                    }
                }
                2 -> {
                    val qrText = "http://${getLocalIp(context)}:8888/"
                    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment=Alignment.CenterHorizontally){
                        Text("QR Transfer")
                        Card(Modifier.size(280.dp)){ Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center){ Text(qrText) } }
                        Text(qrText)
                    }
                }
                3 -> {
                    val history = historyDao.getAll()
                    LazyColumn(Modifier.fillMaxSize().padding(16.dp)){
                        item{ Text("Stats: Отправлено ${statsManager.getTotalSent()} Принято ${statsManager.getTotalReceived()}") }
                        items(history){ Text(it.fileName) }
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
