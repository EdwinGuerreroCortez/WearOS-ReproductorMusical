package com.kronnoz.reproductormusica.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.IconButton
import androidx.navigation.compose.rememberNavController

@Composable
fun PantallaPrincipal(navController: NavController) {
    // State for button animation
    var isPlayPressed by remember { mutableStateOf(false) }
    var isListPressed by remember { mutableStateOf(false) }
    var isPreviousPressed by remember { mutableStateOf(false) }
    var isNextPressed by remember { mutableStateOf(false) }

    // Animation scales
    val playScale by animateFloatAsState(if (isPlayPressed) 0.9f else 1f)
    val listScale by animateFloatAsState(if (isListPressed) 0.9f else 1f)
    val previousScale by animateFloatAsState(if (isPreviousPressed) 0.9f else 1f)
    val nextScale by animateFloatAsState(if (isNextPressed) 0.9f else 1f)

    // Color palette
    val primaryColor = Color(0xFF1DB954) // Spotify-like green
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF1A1A1A))
    )

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
            // Lista button (top-right, adjusted position)
            IconButton(
                onClick = {
                    isListPressed = true
                    navController.navigate("lista")
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-25).dp) // Move inward to avoid system UI overlap
                    .offset(y = (20).dp) // Move inward to avoid system UI overlap
                    .size(20.dp) // Reduced size further
                    .clip(CircleShape)
                    .scale(listScale)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Lista",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp) // Adjusted icon size
                )
            }

            // Play/Pause button (center)
            IconButton(
                onClick = { isPlayPressed = !isPlayPressed /* Toggle play/pause */ },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .scale(playScale)
            ) {
                Icon(
                    imageVector = if (isPlayPressed) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlayPressed) "Reproducir" else "Pausar",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Previous button (left)
            IconButton(
                onClick = { isPreviousPressed = true /* Previous track */ },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
                    .scale(previousScale)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Anterior",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Next button (right)
            IconButton(
                onClick = { isNextPressed = true /* Next track */ },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
                    .scale(nextScale)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Song info (bottom)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color(0x80000000), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Canción 2",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(
    name = "Pantalla Principal (320x320)",
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    showBackground = true
)
@Composable
fun PreviewPantallaPrincipalSmall() {
    PantallaPrincipal(navController = rememberNavController())
}

@Preview(
    name = "Pantalla Principal (454x454)",
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    showBackground = true
)
@Composable
fun PreviewPantallaPrincipalLarge() {
    PantallaPrincipal(navController = rememberNavController())
}