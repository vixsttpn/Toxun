
package com.toxun.share.feedback
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
@Composable
fun WaveAnimation(modifier: Modifier = Modifier){
    val infinite = rememberInfiniteTransition(label="wave")
    val radius by infinite.animateFloat(initialValue=0f, targetValue=300f, animationSpec=infiniteRepeatable(tween(2000, easing=LinearEasing), RepeatMode.Restart), label="r")
    val alpha by infinite.animateFloat(initialValue=0.5f, targetValue=0f, animationSpec=infiniteRepeatable(tween(2000), RepeatMode.Restart), label="a")
    Canvas(modifier=modifier){
        drawCircle(color=Color(0xFF6200EE).copy(alpha=alpha), radius=radius, center=Offset(size.width/2, size.height/2), style=Stroke(width=2f))
    }
}
