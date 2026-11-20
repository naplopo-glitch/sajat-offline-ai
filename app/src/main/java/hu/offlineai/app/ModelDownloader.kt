package hu.offlineai.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object ModelDownloader {

    const val MODEL_FILE_NAME = "Qwen3-0.6B.litertlm"

    private const val MODEL_URL =
        "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"

    private const val EXPECTED_SIZE = 613722264L

    private const val EXPECTED_SHA256 =
        "c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108"

    fun modelFile(context: Context): File {
        return File(context.filesDir, MODEL_FILE_NAME)
    }

    suspend fun download(
        context: Context,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val destination = modelFile(context)

        if (destination.exists() &&
            destination.length() == EXPECTED_SIZE &&
            sha256(destination) == EXPECTED_SHA256
        ) {
            onProgress(100)
            return@withContext destination
        }

        destination.delete()

        val temporary = File(
            context.filesDir,
            "$MODEL_FILE_NAME.part"
        )

        temporary.delete()

        val connection =
            URL(MODEL_URL).openConnection() as HttpURLConnection

        try {
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            connection.instanceFollowRedirects = true

            connection.connect()

            if (connection.responseCode != 200) {
                throw Exception(
                    "Letöltési hiba: HTTP ${connection.responseCode}"
                )
            }

            if (connection.contentLengthLong != EXPECTED_SIZE) {
                throw Exception("A modell fájlmérete nem megfelelő.")
            }

            var downloaded = 0L

            connection.inputStream.use { input ->
                FileOutputStream(temporary).use { output ->

                    val buffer = ByteArray(65536)

                    while (true) {
                        val count = input.read(buffer)

                        if (count == -1) break

                        output.write(buffer, 0, count)
                        downloaded += count

                        val progress =
                            (downloaded * 100 / EXPECTED_SIZE)
                                .toInt()
                                .coerceIn(0, 99)

                        onProgress(progress)
                    }

                    output.fd.sync()
                }
            }

            if (temporary.length() != EXPECTED_SIZE) {
                throw Exception("A letöltés hiányos.")
            }

            if (sha256(temporary) != EXPECTED_SHA256) {
                throw Exception("A modell ellenőrzése sikertelen.")
            }

            if (!temporary.renameTo(destination)) {
                temporary.copyTo(destination, overwrite = true)
                temporary.delete()
            }

            onProgress(100)

            destination

        } catch (error: Exception) {
            temporary.delete()
            throw error

        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().use { input ->
            val buffer = ByteArray(65536)

            while (true) {
                val count = input.read(buffer)

                if (count == -1) break

                digest.update(buffer, 0, count)
            }
        }

        return digest.digest().joinToString("") {
            "%02x".format(it)
        }
    }
}
