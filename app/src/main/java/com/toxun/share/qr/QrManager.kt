
package com.toxun.share.qr
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
class QrManager{
    fun generateQr(text:String, size:Int=512): Bitmap? {
        return try{
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for(x in 0 until size){ for(y in 0 until size){ bmp.setPixel(x,y, if(bitMatrix.get(x,y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()) } }
            bmp
        }catch(e:Exception){ null }
    }
    fun generateWifiQr(ip:String, fileName:String): Bitmap? {
        return generateQr("TOXUN:$ip:8888:$fileName")
    }
}
