package com.kronnoz.reproductormusica.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import com.google.android.gms.wearable.Wearable
import com.kronnoz.reproductormusica.presentation.cancionActual
import kotlinx.coroutines.tasks.await

@Composable
fun PantallaPrincipal(navController: NavController) {
    val primaryColor = Color(0xFF1DB954)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1A1A1A))
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    var nodeId by remember { mutableStateOf<String?>(null) }
    var estadoConexion by remember { mutableStateOf("Buscando conexión...") }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isNotEmpty()) {
                nodeId = nodes[0].id
                estadoConexion = "🔗 Conectado a ${nodes[0].displayName}"
            } else {
                estadoConexion = "❌ No hay nodos conectados"
            }
        } catch (e: Exception) {
            estadoConexion = "❌ Error: ${e.message}"
        }
    }

    Scaffold(
        timeText = {
            TimeText(
                timeTextStyle = TimeTextDefaults.timeTextStyle(
                    color = Color.White,
                    fontSize = 10.sp
                )
            )
        },
        modifier = Modifier.background(backgroundGradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            // Botón de lista
            IconButton(
                onClick = { navController.navigate("lista") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-25).dp, y = 20.dp)
                    .size(20.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Lista",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón play/pausa
            IconButton(
                onClick = {
                    nodeId?.let {
                        val path = if (isPlaying) "/pause" else "/play"
                        enviarMensajeAlCelular(context, it, path)
                        isPlaying = !isPlaying
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Botón anterior
            IconButton(
                onClick = {
                    nodeId?.let { enviarMensajeAlCelular(context, it, "/prev") }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Botón siguiente
            IconButton(
                onClick = {
                    nodeId?.let { enviarMensajeAlCelular(context, it, "/next") }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Título y autor
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF121212), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Ícono musical",
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = cancionActual.value.titulo,
                        color = Color.White,
                        fontSize = 8.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp)) // Added spacer to separate title and author
                Text(
                    text = cancionActual.value.autor,
                    color = Color.Gray,
                    fontSize = 8.sp,
                    maxLines = 1
                )
            }

            Text(
                text = estadoConexion,
                color = Color.LightGray,
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
        }
    }
}

fun enviarMensajeAlCelular(context: android.content.Context, nodeId: String, path: String) {
    Wearable.getMessageClient(context)
        .sendMessage(nodeId, path, null)
}