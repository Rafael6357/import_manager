package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lib.BackupHelper
import com.example.ui.MainViewModel
import com.example.ui.components.AppLogo
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()



    // Dialog trigger states
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog1 by remember { mutableStateOf(false) }
    var showDeleteAllDialog2 by remember { mutableStateOf(false) }
    var showRestoreSummaryDialog by remember { mutableStateOf<BackupHelper.BackupData?>(null) }
    var rawRestoreJsonText by remember { mutableStateOf("") }

    // Backup restore launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytes()
                        val text = String(bytes)
                        val data = BackupHelper.deserializeBackup(text)
                        rawRestoreJsonText = text
                        showRestoreSummaryDialog = data
                    }
                } catch (e: SecurityException) {
                    viewModel.showToast("Error de seguridad al acceder al archivo.")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ajustes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configuraciones, copias de seguridad y seguridad local",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            AppLogo(size = 44.dp)
        }

        // Section: Resumen de datos
        SettingsSection(title = "Contenido de Datos") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("settings_record_counters"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Registros Guardados",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Badge 1: Compras
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "COMPRAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${purchases.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Badge 2: Ventas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "VENTAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${sales.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Respaldo y Restauración
        SettingsSection(title = "Respaldo y Restauración") {
            SettingsRow(
                title = "Exportar Respaldo",
                description = "Genera un archivo de respaldo con firma SHA-256 en la carpeta de Descargas del celular.",
                icon = Icons.Default.Backup,
                testTag = "settings_export_backup",
                onClick = { viewModel.exportBackup(context) }
            )
            
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

            SettingsRow(
                title = "Restaurar Respaldo",
                description = "Selecciona un archivo .json exportado anteriormente para importar todos tus registros.",
                icon = Icons.Default.Restore,
                testTag = "settings_restore_backup",
                onClick = { filePickerLauncher.launch("application/json") }
            )

            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

            SettingsRow(
                title = "Cargar Datos de Prueba",
                description = "Genera automáticamente 8 productos importados de muestra con categorías, pesos, ventas y costos para pruebas.",
                icon = Icons.Default.DeveloperMode,
                testTag = "settings_load_test_data",
                onClick = { viewModel.loadTestData() }
            )
        }



        Spacer(modifier = Modifier.height(16.dp))

        // Section: Seguridad
        SettingsSection(title = "Seguridad") {
            SettingsRow(
                title = "Cambiar PIN",
                description = "Modifica tu PIN de 4 dígitos de acceso local.",
                icon = Icons.Default.Lock,
                testTag = "settings_change_pin",
                onClick = { showChangePinDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Peligro / Datos
        SettingsSection(title = "Gestión de Datos") {
            SettingsRow(
                title = "Eliminar todos los datos",
                description = "Borra permanentemente todas las compras, fotos, ventas y resetea tu PIN de seguridad local. No se puede deshacer.",
                icon = Icons.Default.DeleteForever,
                iconColor = MaterialTheme.colorScheme.error,
                testTag = "settings_delete_all",
                onClick = { showDeleteAllDialog1 = true }
            )
        }

        Spacer(modifier = Modifier.height(56.dp))
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmNewPin by remember { mutableStateOf("") }
        var errorTxt by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Cambiar PIN de Acceso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it },
                        label = { Text("PIN Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().testTag("pin_current_input")
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("Nuevo PIN (4 dígitos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().testTag("pin_new_input")
                    )

                    OutlinedTextField(
                        value = confirmNewPin,
                        onValueChange = { confirmNewPin = it },
                        label = { Text("Confirmar Nuevo PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().testTag("pin_new_confirm_input")
                    )

                    if (errorTxt.isNotEmpty()) {
                        Text(text = errorTxt, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentPin.length != 4 || newPin.length != 4 || confirmNewPin.length != 4) {
                            errorTxt = "Todos los campos de PIN deben tener 4 dígitos."
                            return@TextButton
                        }
                        if (newPin != confirmNewPin) {
                            errorTxt = "Los nuevos PINs no coinciden."
                            return@TextButton
                        }
                        val ok = viewModel.changePin(currentPin, newPin)
                        if (ok) {
                            showChangePinDialog = false
                        } else {
                            errorTxt = "El PIN actual ingresado es incorrecto."
                        }
                    }
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete All Dialog 1
    if (showDeleteAllDialog1) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog1 = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar todos los datos") },
            text = { Text("Se borrarán todas tus compras, registros de ventas y se desvinculará el PIN de forma IRREVERSIBLE. ¿Estás absolutamente seguro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog1 = false
                        showDeleteAllDialog2 = true
                    }
                ) {
                    Text("Sí, proceder", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog1 = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete All Dialog 2
    if (showDeleteAllDialog2) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog2 = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("DOBLE CONFIRMACIÓN REQUERIDA") },
            text = { Text("¿Deseas resetear la app por completo de fábrica y perder todos tus inventarios?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog2 = false
                        viewModel.resetApp()
                    }
                ) {
                    Text("CONFIRMAR Y ELIMINAR TODO", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog2 = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Backup Restore Summary Dialog (UC-07)
    if (showRestoreSummaryDialog != null) {
        val backupData = showRestoreSummaryDialog!!
        AlertDialog(
            onDismissRequest = { showRestoreSummaryDialog = null },
            icon = { Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Resumen de Respaldo Encontrado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Se han encontrado los siguientes datos en el archivo:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Compras: ${backupData.purchases.size}", fontWeight = FontWeight.Bold)
                    Text("• Ventas: ${backupData.sales.size}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("¡PRECAUCIÓN! Al confirmar, se eliminarán todos los registros locales existentes y se reemplazarán con este respaldo.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importBackup(rawRestoreJsonText)
                        showRestoreSummaryDialog = null
                    }
                ) {
                    Text("Confirmar e Importar", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreSummaryDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
