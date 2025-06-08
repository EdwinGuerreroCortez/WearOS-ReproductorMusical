package com.kronnoz.reproductormusica

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object GlobalState {
    val mensajeEnvio = mutableStateOf("")
    val songList = mutableStateListOf<String>()
}