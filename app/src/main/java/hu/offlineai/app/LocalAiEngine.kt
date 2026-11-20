package hu.offlineai.app

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

object LocalAiEngine {

    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    suspend fun initialize(context: Context) {
        withContext(dispatcher) {
            if (conversation != null) return@withContext

            val modelFile = ModelDownloader.modelFile(context)

            if (!modelFile.isFile || modelFile.length() == 0L) {
                throw IllegalStateException(
                    "Az AI-modell még nincs letöltve."
                )
            }

            val newEngine = Engine(
                EngineConfig(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath
                )
            )

            try {
                newEngine.initialize()

                val newConversation =
                    newEngine.createConversation(
                        ConversationConfig(
                            systemInstruction = Contents.of(
                                "Te egy segítőkész, magyarul válaszoló offline AI-asszisztens vagy. " +
                                    "Mindig magyarul válaszolj, hacsak a felhasználó mást nem kér."
                            )
                        )
                    )

                engine = newEngine
                conversation = newConversation

            } catch (error: Throwable) {
                try {
                    newEngine.close()
                } catch (_: Throwable) {
                }
                throw error
            }
        }
    }

    suspend fun sendMessage(prompt: String): String {
        require(prompt.isNotBlank()) {
            "Az üzenet nem lehet üres."
        }

        return withContext(dispatcher) {
            val activeConversation = conversation
                ?: throw IllegalStateException(
                    "Az AI-motor még nincs elindítva."
                )

            val answer = StringBuilder()

            activeConversation
                .sendMessageAsync(prompt)
                .collect { response ->
                    answer.append(response.text)
                }

            answer.toString().trim()
        }
    }

    suspend fun close() {
        withContext(dispatcher) {
            conversation?.close()
            conversation = null

            engine?.close()
            engine = null
        }
    }
}
