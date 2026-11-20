
package hu.offlineai.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Yellow = Color(0xFFFFC400)
private val Dark = Color(0xFF090909)
private val Panel = Color(0xFF191919)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OfflineAIApp()
        }
    }
}

@Composable
fun OfflineAIApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    var message by remember {
        mutableStateOf("")
    }

    val messages = remember {
        mutableStateListOf<String>()
    }

    var downloading by remember {
        mutableStateOf(false)
    }

    var checking by remember {
        mutableStateOf(true)
    }

    var progress by remember {
        mutableIntStateOf(0)
    }

    var modelReady by remember {
        mutableStateOf(false)
    }

    var status by remember {
        mutableStateOf("A modell ellenőrzése...")
    }

    LaunchedEffect(context) {
        checking = true

        try {
            modelReady = ModelDownloader.isValid(context)

            status = if (modelReady) {
                "A helyi AI-modell ellenőrizve."
            } else {
                "A helyi AI-modell még nincs letöltve."
            }
        } catch (error: Exception) {
            modelReady = false
            status = "Ellenőrzési hiba: ${error.message}"
        } finally {
            checking = false
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Yellow,
            background = Dark,
            surface = Panel,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Dark)
                .padding(18.dp)
        ) {
            Text(
                text = "OFFLINE AI",
                color = Yellow,
                fontSize = 25.sp
            )

            Text(
                text = "Saját mesterséges intelligenciád",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "● RENDSZER",
                        color = Yellow
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = status,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (downloading) {
                        LinearProgressIndicator(
                            progress = {
                                progress / 100f
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = Yellow,
                            trackColor = Color.DarkGray
                        )

                        Text(
                            text = "Letöltés: $progress%",
                            color = Yellow,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                downloading = true
                                progress = 0
                                status = "AI-modell letöltése folyamatban..."

                                try {
                                    ModelDownloader.download(context) {
                                        value ->
                                        mainHandler.post {
                                            progress = value
                                        }
                                    }

                                    modelReady =
                                        ModelDownloader.isValid(context)

                                    if (!modelReady) {
                                        throw Exception(
                                            "A modell ellenőrzése sikertelen."
                                        )
                                    }

                                    status =
                                        "A modell letöltése és ellenőrzése sikerült."

                                } catch (error: Exception) {
                                    status =
                                        "Hiba: ${error.message ?: "Ismeretlen hiba"}"
                                } finally {
                                    downloading = false
                                }
                            }
                        },
                        enabled = !downloading &&
                            !modelReady &&
                            !checking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Yellow,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.DarkGray,
                            disabledContentColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                checking -> "Ellenőrzés..."
                                modelReady -> "Modell letöltve"
                                downloading -> "Letöltés..."
                                else -> "AI-modell letöltése (kb. 586 MB)"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BESZÉLGETÉS",
                color = Yellow,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Panel
                        )
                    ) {
                        Text(
                            text = item,
                            color = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Írj valamit...")
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yellow,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Yellow
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            messages.add("Te: $message")
                            messages.add(
                                "Az AI-motor még nincs csatlakoztatva."
                            )
                            message = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Küldés")
                }
            }
        }
    }
}
