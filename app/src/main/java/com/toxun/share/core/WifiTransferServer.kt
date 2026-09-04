
package com.toxun.share.core
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
data class TransferProgress(val sent:Long, val total:Long, val speedBps:Long)
class WifiTransferServer(private val port:Int=8888){
    private var job: Job?=null
    val progress = MutableStateFlow<TransferProgress?>(null)
    var currentFile: File?=null
    fun start(file: File){
        currentFile=file
        job=CoroutineScope(Dispatchers.IO).launch {
            try{
                val server = ServerSocket(port)
                while(isActive){
                    val client = server.accept()
                    launch {
                        try{
                            val out = client.getOutputStream()
                            val input = FileInputStream(file)
                            val header = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ${file.length()}\r\nContent-Disposition: attachment; filename=\"${file.name}\"\r\n\r\n"
                            out.write(header.toByteArray())
                            val buf = ByteArray(256*1024)
                            var total=0L
                            val start = System.currentTimeMillis()
                            while(true){ val r = input.read(buf); if(r==-1) break; out.write(buf,0,r); total+=r; val elapsed=(System.currentTimeMillis()-start)/1000.0; val speed=if(elapsed>0)(total/elapsed).toLong() else 0L; progress.value=TransferProgress(total,file.length(),speed) }
                            input.close(); out.flush(); client.close()
                        }catch(_:Exception){}
                    }
                }
            }catch(_:Exception){}
        }
    }
    fun stop(){ job?.cancel(); job=null }
    fun getUrl(ip:String): String = "http://$ip:$port/"
}
