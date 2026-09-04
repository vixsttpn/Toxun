
package com.toxun.share.ui
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.toxun.share.ui.screens.MainScreen
import com.toxun.share.ui.theme.ToxunTheme
class MainActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent{ ToxunTheme{ MainScreen() } }
    }
}
