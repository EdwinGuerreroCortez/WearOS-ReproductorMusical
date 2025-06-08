package com.kronnoz.reproductormusica.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.kronnoz.reproductormusica.model.SongItem

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var canciones: List<SongItem> = emptyList()
    private var currentIndex = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_PLAY_SONG" -> {
                val id = intent.getStringExtra("SONG_ID")
                if (id != null) {
                    cargarCancionesSiEsNecesario()
                    val index = canciones.indexOfFirst { it.id == id }
                    if (index != -1) {
                        currentIndex = index
                        reproducirCancion(canciones[currentIndex])
                    }
                }
            }

            "ACTION_PLAY" -> {
                mediaPlayer?.start()
            }

            "ACTION_PAUSE" -> {
                mediaPlayer?.pause()
            }

            "ACTION_NEXT" -> {
                if (canciones.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % canciones.size
                    reproducirCancion(canciones[currentIndex])
                }
            }

            "ACTION_PREV" -> {
                if (canciones.isNotEmpty()) {
                    currentIndex = if (currentIndex - 1 < 0) canciones.size - 1 else currentIndex - 1
                    reproducirCancion(canciones[currentIndex])
                }
            }
        }
        return START_STICKY
    }

    private fun cargarCancionesSiEsNecesario() {
        if (canciones.isNotEmpty()) return

        val lista = mutableListOf<SongItem>()
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
                lista.add(
                    SongItem(
                        id = it.getString(idColumn),
                        title = it.getString(titleColumn),
                        artist = it.getString(artistColumn)
                    )
                )
            }
        }

        canciones = lista
    }

    private fun reproducirCancion(song: SongItem) {
        mediaPlayer?.release()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
            .appendPath(song.id)
            .build()

        mediaPlayer = MediaPlayer.create(this, uri)
        mediaPlayer?.start()

        enviarCancionActualAlReloj(song)
    }

    private fun enviarCancionActualAlReloj(song: SongItem) {
        val map = mapOf("title" to song.title, "artist" to song.artist)
        val json = Gson().toJson(map)

        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this)
                    .sendMessage(node.id, "/current_song", json.toByteArray())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
