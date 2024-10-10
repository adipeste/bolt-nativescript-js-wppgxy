package org.nativescript.samplejs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MapDownloadViewModel(private val context: Context) : ViewModel() {
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    val availableRegions = listOf("North America", "Europe", "Asia", "Africa", "South America", "Oceania")

    fun downloadMap(region: String) {
        viewModelScope.launch {
            val url = URL("https://example.com/maps/$region.mbtiles") // Replace with actual map source
            val connection = url.openConnection()
            val fileLength = connection.contentLength

            val inputStream = connection.getInputStream()
            val file = File(context.filesDir, "$region.mbtiles")
            val outputStream = FileOutputStream(file)

            var bytesRead = 0
            val buffer = ByteArray(8192)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                outputStream.write(buffer, 0, bytes)
                bytesRead += bytes
                _downloadProgress.value = bytesRead.toFloat() / fileLength
                bytes = inputStream.read(buffer)
            }

            outputStream.close()
            inputStream.close()

            saveMapToDatabase(region)
        }
    }

    private suspend fun saveMapToDatabase(region: String) {
        val mapDao = MapDatabase.getDatabase(context).mapDao()
        mapDao.insert(MapEntity(region = region, downloadDate = System.currentTimeMillis()))
    }
}