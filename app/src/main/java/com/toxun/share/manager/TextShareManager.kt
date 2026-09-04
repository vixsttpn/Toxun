
package com.toxun.share.manager
import android.content.Context
import android.content.Intent
class TextShareManager{
    fun shareText(context: Context, text: String){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Отправить текст"))
    }
}
