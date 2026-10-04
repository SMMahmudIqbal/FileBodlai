package com.example.fileconverter.util

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class RecentFile(
    val id: String,
    val fileName: String,
    val uriString: String,
    val mimeType: String,
    val timestamp: Long,
    val typeLabel: String
)

object HistoryManager {
    private const val PREFS_NAME = "file_converter_history"
    private const val KEY_RECENTS = "recents_json"
    private const val MAX_RECENTS = 25

    fun addRecent(context: Context, fileName: String, uri: Uri, mimeType: String, typeLabel: String) {
        val list = getRecents(context).toMutableList()
        val item = RecentFile(
            id = "${System.currentTimeMillis()}_${(0..1000).random()}",
            fileName = fileName,
            uriString = uri.toString(),
            mimeType = mimeType,
            timestamp = System.currentTimeMillis(),
            typeLabel = typeLabel
        )
        // Avoid duplicate by uriString
        list.removeAll { it.uriString == uri.toString() }
        list.add(0, item)
        val trimmed = if (list.size > MAX_RECENTS) list.take(MAX_RECENTS) else list
        saveRecents(context, trimmed)
    }

    fun getRecents(context: Context): List<RecentFile> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        val result = mutableListOf<RecentFile>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    RecentFile(
                        id = obj.getString("id"),
                        fileName = obj.getString("fileName"),
                        uriString = obj.getString("uriString"),
                        mimeType = obj.getString("mimeType"),
                        timestamp = obj.getLong("timestamp"),
                        typeLabel = obj.optString("typeLabel", "File")
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }

    fun removeRecent(context: Context, id: String) {
        val list = getRecents(context).filterNot { it.id == id }
        saveRecents(context, list)
    }

    fun clearAll(context: Context) {
        saveRecents(context, emptyList())
    }

    private fun saveRecents(context: Context, list: List<RecentFile>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("fileName", item.fileName)
                put("uriString", item.uriString)
                put("mimeType", item.mimeType)
                put("timestamp", item.timestamp)
                put("typeLabel", item.typeLabel)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENTS, array.toString())
            .apply()
    }
}
