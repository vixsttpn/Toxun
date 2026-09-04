
package com.toxun.share.data.history
import android.content.Context
data class HistoryItem(val fileName:String, val timestamp:Long, val size:Long, val isSent:Boolean)
class HistoryDao(context: Context){
    private val prefs = context.getSharedPreferences("toxun_history", Context.MODE_PRIVATE)
    fun add(item: HistoryItem){
        val existing = prefs.getStringSet("items", emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add("${item.fileName}|${item.timestamp}|${item.size}|${item.isSent}")
        prefs.edit().putStringSet("items", existing).apply()
    }
    fun getAll(): List<HistoryItem>{
        val set = prefs.getStringSet("items", emptySet()) ?: return emptyList()
        return set.mapNotNull { s ->
            try{
                val parts = s.split("|")
                HistoryItem(parts[0], parts[1].toLong(), parts[2].toLong(), parts[3].toBoolean())
            }catch(e:Exception){ null }
        }.sortedByDescending { it.timestamp }
    }
}
