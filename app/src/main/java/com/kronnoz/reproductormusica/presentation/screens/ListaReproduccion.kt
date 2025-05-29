package com.kronnoz.reproductormusica.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ListaReproduccion(navController: NavController) {
    val canciones = listOf(
        "Canción 1", "Canción 2", "Canción 3", "Canción 4", "Canción 5",
        "Canción 6", "Canción 7", "Canción 8", "Canción 9", "Canción 10"
    )

    val primaryColor = Color(0xFF1DB954) // Verde tipo Spotify
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
                    style = MaterialTheme.typography.title1,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(canciones) { cancion ->
                Card(
                    onClick = { /* Acción al presionar */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Fondo personalizado dentro del Card
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(8.dp))
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cancion,
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
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
}

@Preview(
    name = "Lista Reproducción (320x320)",
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    showBackground = true
)
@Composable
fun PreviewListaReproduccionSmall() {
    ListaReproduccion(navController = rememberNavController())
}

@Preview(
    name = "Lista Reproducción (454x454)",
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    showBackground = true
)
@Composable
fun PreviewListaReproduccionLarge() {
    ListaReproduccion(navController = rememberNavController())
}
