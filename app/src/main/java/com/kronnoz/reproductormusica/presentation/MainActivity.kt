package com.kronnoz.reproductormusica.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kronnoz.reproductormusica.presentation.navigation.AppNavigation
import com.kronnoz.reproductormusica.presentation.theme.ReproductorMusicaTheme
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

// Lista de canciones (para ListaReproduccion)
val cancionesGlobal = mutableStateListOf<Cancion>()

// Canción actual (para PantallaPrincipal)
val cancionActual = mutableStateOf(Cancion("0", "Título desconocido", "Autor desconocido"))

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Wearable.getMessageClient(this).addListener(this)

        setContent {
            ReproductorMusicaTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val json = String(messageEvent.data)

        when (path) {
            "/songs_list_response" -> {
                Log.d("Wear", "📥 Canciones recibidas: $json")
                try {
                    val gson = Gson()
                    val type = object : TypeToken<List<Map<String, String>>>() {}.type
                    val songList = gson.fromJson<List<Map<String, String>>>(json, type) ?: emptyList()

                    cancionesGlobal.clear()
                    songList.forEach { song ->
                        val id = song["id"] ?: ""
                        val title = song["title"]?.takeIf { it.isNotBlank() } ?: "Título desconocido"
                        val artist = song["artist"]?.takeIf { it.isNotBlank() } ?: "Autor desconocido"
                        cancionesGlobal.add(Cancion(id = id, titulo = title, autor = artist))
                    }

                    Log.d("Wear", "🎵 Lista actualizada: $cancionesGlobal")

                } catch (e: Exception) {
                    Log.e("Wear", "❌ Error al parsear JSON de lista: ${e.message}")
                }
            }

            "/current_song" -> {
                Log.d("Wear", "📥 Canción actual recibida: $json")
                try {
                    val map = Gson().fromJson(json, Map::class.java)
                    val title = map["title"]?.toString() ?: "Título desconocido"
                    val artist = map["artist"]?.toString() ?: "Autor desconocido"
                    cancionActual.value = Cancion("x", title, artist)
                    Log.d("Wear", "🎶 Canción actual actualizada: ${cancionActual.value}")
                } catch (e: Exception) {
                    Log.e("Wear", "❌ Error al parsear canción actual: ${e.message}")
                }
            }

            else -> {
                Log.w("Wear", "⚠️ Path no reconocido: $path")
            }
        }
    }
}
