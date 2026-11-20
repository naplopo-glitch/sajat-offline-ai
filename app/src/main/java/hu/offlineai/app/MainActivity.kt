
package hu.offlineai.app

import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }

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

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "● RENDSZER",
                        color = Yellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A kezelőfelület elkészült.",
                        color = Color.White
                    )
                    Text(
                        text = "Az offline AI-modell még nincs csatlakoztatva.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
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
                    onValueChange = { message = it },
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
                                "Offline AI: A helyi modell még nincs csatlakoztatva."
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
