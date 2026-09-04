
package com.toxun.share.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val LightColors = lightColorScheme(primary=Color(0xFF000000), secondary=Color(0xFF6200EE), background=Color(0xFFFFFFFF))
@Composable fun ToxunTheme(content: @Composable ()->Unit){ MaterialTheme(colorScheme=LightColors, content=content) }
