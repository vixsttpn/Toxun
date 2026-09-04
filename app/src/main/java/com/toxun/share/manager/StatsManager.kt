
package com.toxun.share.manager
import android.content.Context
class StatsManager(context: Context){
    private val prefs = context.getSharedPreferences("toxun_stats", Context.MODE_PRIVATE)
    fun incSent(){ prefs.edit().putInt("sent", getTotalSent()+1).apply() }
    fun incReceived(){ prefs.edit().putInt("received", getTotalReceived()+1).apply() }
    fun getTotalSent(): Int = prefs.getInt("sent", 0)
    fun getTotalReceived(): Int = prefs.getInt("received", 0)
}
