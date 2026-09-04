
package com.toxun.share.feedback
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
class ShakeDetector(context: Context, private val onShake: ()->Unit): SensorEventListener{
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShake = 0L
    fun start(){ sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI) }
    fun stop(){ sensorManager.unregisterListener(this) }
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]; val y = it.values[1]; val z = it.values[2]
            val gForce = Math.sqrt((x*x + y*y + z*z).toDouble())
            if(gForce > 2.7){
                val now = System.currentTimeMillis()
                if(now - lastShake > 1000){ lastShake=now; onShake() }
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}
}
