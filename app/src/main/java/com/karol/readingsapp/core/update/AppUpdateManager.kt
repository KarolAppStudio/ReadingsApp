package com.karol.readingsapp.core.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(private val context: Context) {

    private val latestReleaseUrl = "https://api.github.com/repos/KarolAppStudio/ReadingsApp/releases/latest"

    sealed class UpdateResult {
        object NoUpdateAvailable : UpdateResult()
        data class NewUpdateAvailable(val version: String, val downloadUrl: String) : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(latestReleaseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "ReadingsApp")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)
                val tagName = jsonObject.getString("tag_name")
                val assets = jsonObject.getJSONArray("assets")
                var downloadUrl: String? = null

                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                val currentVersion = getCurrentVersionName()
                if ((isNewerVersion(tagName, currentVersion)) && downloadUrl != null) {
                    UpdateResult.NewUpdateAvailable(tagName, downloadUrl)
                } else {
                    UpdateResult.NoUpdateAvailable
                }
            } else {
                UpdateResult.Error("Failed to check for updates: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateManager", "Update check failed", e)
            UpdateResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun getCurrentVersionName(): String = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }

    internal fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        val latest = latestVersion.removePrefix("v").trim()
        val current = currentVersion.removePrefix("v").trim()

        if (latest == current) return false

        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }

    fun downloadAndInstall(downloadUrl: String) {
        val destination = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "readings_app_update.apk")
        if (destination.exists()) destination.delete()

        val request = DownloadManager.Request(downloadUrl.toUri())
            .setTitle("Readings App Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destination))

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == downloadId) {
                    installApk(destination)
                    try {
                        context.unregisterReceiver(this)
                    } catch (_: Exception) {}
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            onComplete,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
