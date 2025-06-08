package com.kronnoz.reproductormusica.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import com.kronnoz.reproductormusica.GlobalState
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import android.content.Intent

class SongListResponderService : WearableListenerService() {

    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        Log.d("WearService", "🔧 Servicio SongListResponderService iniciado")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearService", "📩 Solicitud recibida del reloj: ${messageEvent.path}")

        Handler(Looper.getMainLooper()).post {
            GlobalState.mensajeEnvio.value = "📩 Se recibió solicitud del reloj"
        }

        when (messageEvent.path) {

            "/request_songs_list" -> {
                if (checkPermissions()) {
                    val canciones = obtenerListaCancionesJSON()
                    Log.d("WearService", "🎵 Canciones obtenidas: $canciones")

                    val nodeId = messageEvent.sourceNodeId
                    Log.d("WearService", "📡 Enviando canciones al nodo: $nodeId")

                    Wearable.getMessageClient(this)
                        .sendMessage(nodeId, "/songs_list_response", canciones.toByteArray())
                        .addOnSuccessListener {
                            Log.d("WearService", "✅ Lista enviada con éxito al reloj")
                            Handler(Looper.getMainLooper()).post {
                                GlobalState.mensajeEnvio.value = "✅ Canciones enviadas al reloj"
                            }
                        }
                        .addOnFailureListener { error ->
                            Log.e("WearService", "❌ Error al enviar canciones: ${error.message}")
                            Handler(Looper.getMainLooper()).post {
                                GlobalState.mensajeEnvio.value = "❌ Error al enviar canciones: ${error.message}"
                            }
                        }
                } else {
                    Log.e("WearService", "❌ Permisos no otorgados")
                    Handler(Looper.getMainLooper()).post {
                        GlobalState.mensajeEnvio.value = "❌ Permisos no otorgados"
                    }
                }
            }

            "/play_song" -> {
                val songId = String(messageEvent.data)
                Log.d("WearService", "🎶 Reproducir canción con ID: $songId")

                val intent = Intent(this, MusicService::class.java).apply {
                    action = "ACTION_PLAY_SONG"
                    putExtra("SONG_ID", songId)
                }
                startService(intent)
            }

            "/play" -> {
                Log.d("WearService", "▶️ Acción PLAY recibida")
                val intent = Intent(this, MusicService::class.java).apply {
                    action = "ACTION_PLAY"
                }
                startService(intent)
            }

            "/pause" -> {
                Log.d("WearService", "⏸️ Acción PAUSE recibida")
                val intent = Intent(this, MusicService::class.java).apply {
                    action = "ACTION_PAUSE"
                }
                startService(intent)
            }

            "/next" -> {
                Log.d("WearService", "⏭️ Acción NEXT recibida")
                val intent = Intent(this, MusicService::class.java).apply {
                    action = "ACTION_NEXT"
                }
                startService(intent)
            }

            "/prev" -> {
                Log.d("WearService", "⏮️ Acción PREV recibida")
                val intent = Intent(this, MusicService::class.java).apply {
                    action = "ACTION_PREV"
                }
                startService(intent)
            }


            else -> {
                Log.w("WearService", "⚠️ Acción desconocida: ${messageEvent.path}")
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, readPermission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
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
            Log.w("WearService", "⚠️ Cursor es null, no se encontraron canciones")
        }

        return gson.toJson(songs)
    }
}
