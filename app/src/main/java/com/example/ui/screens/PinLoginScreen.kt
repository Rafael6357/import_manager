package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.AppLogo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun PinLoginScreen(viewModel: MainViewModel) {
    var pinText by remember { mutableStateOf("") }
    val isLocked by viewModel.isLocked.collectAsState()
    val lockoutRemainingSecs by viewModel.lockoutRemainingSecs.collectAsState()
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showDoubleConfirmDialog by remember { mutableStateOf(false) }

    fun handleDigit(digit: String) {
        if (isLocked) return
        val updated = pinText + digit
        if (updated.length <= 4) {
            pinText = updated
        }

        if (updated.length == 4) {
            val verified = viewModel.verifyPin(updated)
            pinText = "" // Clear after submission
        }
    }

    fun handleBackspace() {
        if (isLocked) return
        if (pinText.isNotEmpty()) {
            pinText = pinText.dropLast(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper block
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            AppLogo(size = 96.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ImportManager",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ingresa tu PIN de seguridad",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLocked) {
                Text(
                    text = "Teclado bloqueado por seguridad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Inténtalo de nuevo en $lockoutRemainingSecs segundos",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                // PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val filled = i < pinText.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (filled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
            // Elevate the dots by adding extra spacer at the bottom of Upper block
            Spacer(modifier = Modifier.height(36.dp))
        }

        // Mid custom visual keypad (Optimized, more compact to avoid bottom clipping)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = listOf(
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "", "0", "back"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(0.80f)
            ) {
                items(keys.size) { index ->
                    val key = keys[index]
                    val enabled = !isLocked && key.isNotEmpty()
                    when {
                        key == "back" -> {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLocked) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable(enabled = !isLocked) { handleBackspace() }
                                    .testTag("keypad_back"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Borrar",
                                    tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        key.isEmpty() -> {
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLocked) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable(enabled = !isLocked) { handleDigit(key) }
                                    .testTag("keypad_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Forgot PIN text
        Text(
            text = "¿Olvidaste tu PIN?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .clickable { showResetDialog = true }
                .padding(vertical = 12.dp)
                .testTag("forgot_pin_button")
        )
    }

    // Reset Dialog (First warning)
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Olvidó su PIN?") },
            text = {
                Text("Si restablece el PIN, se borrarán de forma irreversible todos los datos almacenados de forma local (compras, ventas y reportes). ¿Desea proceder?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        showDoubleConfirmDialog = true
                    }
                ) {
                    Text("Sí, continuar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Double Confirm Dialog (Second warning)
    if (showDoubleConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDoubleConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("CONFIRMAR ELIMINACIÓN TOTAL") },
            text = {
                Text("Esta acción NO tiene marcha atrás. ¿Seguro que quiere borrar TODAS las compras, ventas y datos para crear un nuevo PIN?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDoubleConfirmDialog = false
                        viewModel.resetApp()
                    }
                ) {
                    Text("Borrar TODO y restablecer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDoubleConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
