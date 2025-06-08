package com.kronnoz.reproductormusica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.kronnoz.reproductormusica.ui.theme.ReproductorMusicaTheme

class DebuggerActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private val estadoMensaje = mutableStateOf("⏳ Esperando mensaje del reloj...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Wearable.getMessageClient(this).addListener(this)

        setContent {
            ReproductorMusicaTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧪 Estado del Servicio", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(estadoMensaje.value, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            estadoMensaje.value = "⏳ Esperando mensaje del reloj..."
                        }) {
                            Text("Resetear estado")
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_songs_list") {
            estadoMensaje.value = "✅ Mensaje recibido correctamente desde el reloj"
        } else {
            estadoMensaje.value = "⚠️ Mensaje desconocido: ${messageEvent.path}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }
}
