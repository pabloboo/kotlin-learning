package com.pabloboo.runtracker.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.ContextCompat.getString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.ui.viewmodels.MainViewModel
import com.pabloboo.runtracker.utils.Constants.ERROR_MESSAGE
import com.pabloboo.runtracker.utils.Constants.SUCCESS_MESSAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ExportAndImportData {

    suspend fun exportRunsToJson(context: Context, runs: List<Run>): SnackbarMessage {
        var result = SnackbarMessage("", SUCCESS_MESSAGE)
        withContext(Dispatchers.IO) {
            val gson = Gson()
            val serializableRuns = runs.map { run ->
                SerializableRun(
                    img = run.img?.let { encodeBitmapToBase64(it) },
                    timestamp = run.timestamp,
                    avgSpeedInKMH = run.avgSpeedInKMH,
                    distanceInMeters = run.distanceInMeters,
                    timeInMillis = run.timeInMillis,
                    caloriesBurned = run.caloriesBurned,
                    id = run.id
                )
            }
            val jsonString = gson.toJson(serializableRuns)
            Timber.d("exportRunsToJson: ${jsonString.length}, $jsonString")
            result = exportRunsToDownloads(context, jsonString)
        }
        return result
    }

    private fun exportRunsToDownloads(context: Context, jsonString: String, fileName: String = "runs.json"): SnackbarMessage {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and higher: Use MediaStore to save to the Downloads folder
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                try {
                    resolver.openOutputStream(uri).use { outputStream ->
                        outputStream?.write(jsonString.toByteArray())
                        outputStream?.flush()
                    }
                    Timber.d("File saved correctly in the Downloads folder.")
                    return SnackbarMessage(getString(context, R.string.runs_saved_in) + " ${uri.path}", SUCCESS_MESSAGE)
                } catch (e: IOException) {
                    Timber.e("Error when writing to the file: ${e.message}")
                    return SnackbarMessage(getString(context, R.string.error_when_writing_to_the_file), ERROR_MESSAGE)
                }
            } else {
                Timber.e("The file couldn't be created in Media Store.")
                return SnackbarMessage(getString(context, R.string.the_file_couldnt_be_created), ERROR_MESSAGE)
            }
        } else {
            // Android 9 and lower: Check permissions and save directly to the Downloads folder
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                try {
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                        outputStream.flush()
                    }
                    Timber.d("File saved correctly in the Downloads folder.")
                    return SnackbarMessage(getString(context, R.string.runs_saved_in)+ " ${file.path}", SUCCESS_MESSAGE)
                } catch (e: IOException) {
                    Timber.e("Error when writing to the file: ${e.message}")
                    return SnackbarMessage(getString(context, R.string.error_when_writing_to_the_file), ERROR_MESSAGE)
                }
            } else {
                Timber.e("Write permission not granted. Cannot save the file.")
                return SnackbarMessage(getString(context, R.string.write_permission_not_granted), ERROR_MESSAGE)
            }
        }
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    suspend fun importRunsFromJson(context: Context, viewModel: MainViewModel, fileUri: Uri): SnackbarMessage {
        val result = SnackbarMessage("", SUCCESS_MESSAGE)
        withContext(Dispatchers.IO) {
            try {
                // Read JSON file
                val jsonString = context.contentResolver.openInputStream(fileUri)?.bufferedReader().use { it?.readText() }
                if (jsonString != null) {
                    // Deserialize JSON
                    val gson = Gson()
                    val type = object : TypeToken<List<SerializableRun>>() {}.type
                    val serializableRuns: List<SerializableRun> = gson.fromJson(jsonString, type)

                    // Convert SerializableRun to Run and save in Room
                    val runs = mutableListOf<Run>()
                    serializableRuns.map { serializableRun ->
                        val run = serializableRun.id?.let { viewModel.getRunById(it) }
                        Timber.d("run: ${run?.id}")
                        if (run == null) {
                            runs.add(
                                Run(
                                    img = serializableRun.img?.let { decodeBase64ToBitmap(it) },
                                    timestamp = serializableRun.timestamp,
                                    avgSpeedInKMH = serializableRun.avgSpeedInKMH,
                                    distanceInMeters = serializableRun.distanceInMeters,
                                    timeInMillis = serializableRun.timeInMillis,
                                    caloriesBurned = serializableRun.caloriesBurned)
                            )
                        }
                    }

                    Timber.d("runs: ${runs.size}")
                    viewModel.insertRuns(runs)
                    result.message = getString(context, R.string.runs_imported_correctly)
                    result.type = SUCCESS_MESSAGE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                result.message = getString(context, R.string.error_importing_runs)
                result.type = ERROR_MESSAGE
            }
        }
        return result
    }

    private fun decodeBase64ToBitmap(encodedString: String): Bitmap? {
        val decodedString = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

}

data class SerializableRun(
    var img: String? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
    var id: Int? = null
)
