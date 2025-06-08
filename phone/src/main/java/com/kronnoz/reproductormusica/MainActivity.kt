package com.kronnoz.reproductormusica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.Wearable
import com.kronnoz.reproductormusica.ui.theme.ReproductorMusicaTheme
import kotlinx.coroutines.launch
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        solicitarPermisos()

        setContent {
            ReproductorMusicaTheme {
                val mensaje = GlobalState.mensajeEnvio
                var nodosConectados by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()
                var canciones by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

                LaunchedEffect(Unit) {
                    if (checkPermissions()) {
                        val json = obtenerListaCancionesJSON()
                        canciones = parseSongs(json)
                    }
                }

                LaunchedEffect(Unit) {
                    scope.launch {
                        Wearable.getNodeClient(this@MainActivity).connectedNodes
                            .addOnSuccessListener { nodes ->
                                nodosConectados = if (nodes.isNotEmpty()) {
                                    "🔗 Nodo conectado: ${nodes.joinToString { it.displayName }}"
                                } else {
                                    "🚫 Ningún nodo conectado"
                                }
                            }
                            .addOnFailureListener {
                                nodosConectados = "❌ Error al obtener nodos: ${it.message}"
                            }
                    }
                }

                LaunchedEffect(mensaje.value) {
                    if (mensaje.value.isNotEmpty()) {
                        kotlinx.coroutines.delay(6000)
                        GlobalState.mensajeEnvio.value = ""
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "📡 Estado de conexión con el reloj:",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = nodosConectados,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (mensaje.value.isNotEmpty()) {
                            Text(
                                text = "📨 Último estado: ${mensaje.value}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "🎵 Lista de canciones en el teléfono:",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn {
                            items(canciones) { song ->
                                Text(
                                    text = "${song["title"]} - ${song["artist"]}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = {
                            GlobalState.mensajeEnvio.value = "🔘 Simulación de envío manual exitosa"
                        }) {
                            Text("Probar mensaje local")
                        }
                    }
                }
            }
        }
    }

    private fun solicitarPermisos() {
        val permisos = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val noOtorgados = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (noOtorgados.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, noOtorgados.toTypedArray(), 100)
        }
    }

    private fun checkPermissions(): Boolean {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, readPermission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun obtenerListaCancionesJSON(): String {
        val songs = mutableListOf<Map<String, String>>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (it.moveToNext()) {
                val song = mapOf(
                    "id" to it.getString(idColumn),
                    "title" to it.getString(titleColumn),
                    "artist" to it.getString(artistColumn)
                )
                songs.add(song)
            }
        } ?: run {
            Log.w("MainActivity", "Cursor is null, no songs found")
        }

        return Gson().toJson(songs)
    }

    private fun parseSongs(jsonString: String): List<Map<String, String>> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parsing JSON: ${e.message}")
            emptyList()
        }
    }
}