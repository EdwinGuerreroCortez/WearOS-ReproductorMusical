package com.kronnoz.reproductormusica.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.google.android.gms.wearable.Wearable
import com.kronnoz.reproductormusica.presentation.cancionesGlobal
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ListaReproduccion(navController: NavController) {
    val primaryColor = Color(0xFF1DB954)
    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFF121212), Color(0xFF1A1A1A))
    )

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var receiveStatus by remember { mutableStateOf("Recibiendo datos...") }

    LaunchedEffect(Unit) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isNotEmpty()) {
                val nodeId = nodes[0].id
                Wearable.getMessageClient(context)
                    .sendMessage(nodeId, "/request_songs_list", null)
                    .await()
                receiveStatus = "Esperando respuesta..."
            } else {
                receiveStatus = "No hay nodos conectados"
            }
        } catch (e: Exception) {
            receiveStatus = "Error al enviar solicitud: ${e.message}"
        }
    }

    LaunchedEffect(cancionesGlobal.size) {
        receiveStatus = if (cancionesGlobal.isNotEmpty()) {
            "Datos recibidos: ${cancionesGlobal.size} canciones"
        } else {
            "No se recibieron datos"
        }
    }

    val canciones = cancionesGlobal

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
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item {
                Text(
                    text = "Lista de reproducción",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = receiveStatus,
                    fontSize = 12.sp,
                    color = if (receiveStatus.contains("Error")) Color.Red else Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(canciones, key = { it.id }) { cancionItem ->
                var offsetX by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    if (offsetX < 0f) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color.Red.copy(alpha = 0.7f))
                                    )
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(20.dp)
                            )
                        }
                    }

                    Card(
                        onClick = {
                            scope.launch {
                                try {
                                    val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                                    if (nodes.isNotEmpty()) {
                                        Wearable.getMessageClient(context)
                                            .sendMessage(nodes[0].id, "/play_song", cancionItem.id.toByteArray())
                                            .await()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .offset(x = offsetX.dp)
                            .fillMaxWidth()
                            .pointerInput(cancionItem) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (offsetX < -100f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scope.launch {
                                                cancionesGlobal.remove(cancionItem)
                                            }
                                        } else {
                                            offsetX = 0f
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consumeAllChanges()
                                        if (dragAmount.x < 0) offsetX += dragAmount.x
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = cancionItem.titulo,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    BasicText(
                                        text = cancionItem.autor,
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Light
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewListaReproduccionSmall() {
    ListaReproduccion(navController = rememberNavController())
}
