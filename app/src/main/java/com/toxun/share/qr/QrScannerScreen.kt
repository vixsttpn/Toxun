
package com.toxun.share.qr
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
class QrScannerScreen: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent { Text("QR Scanner - используй камеру чтобы отсканировать TOXUN QR") }
    }
}
