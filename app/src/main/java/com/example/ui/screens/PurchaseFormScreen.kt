package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Purchase
import com.example.lib.ImageCompressor
import com.example.ui.MainViewModel
import com.example.ui.components.AppLogo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFormScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editingPurchase by viewModel.currentEditingPurchase.collectAsState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitCostUsd by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Planificado") }
    var purchaseDate by remember { mutableStateOf("") }
    var shippingMethod by remember { mutableStateOf("Por Peso") }
    
    var weightKg by remember { mutableStateOf("") }
    var lengthCm by remember { mutableStateOf("") }
    var widthCm by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    
    var unitShippingCostUsd by remember { mutableStateOf("") }
    var otherExpensesUsd by remember { mutableStateOf("") }
    var potentialSellingPriceUsd by remember { mutableStateOf("") }
    
    var trackingNumber by remember { mutableStateOf("") }
    var shippingAgency by remember { mutableStateOf("") }
    
    var photoBase64 by remember { mutableStateOf<String?>(null) }

    // Dropdown expanded states
    var statusExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    // Init form when editing
    LaunchedEffect(editingPurchase) {
        if (editingPurchase != null) {
            val p = editingPurchase!!
            name = p.name
            category = p.category ?: ""
            description = p.description ?: ""
            quantity = p.quantity.toString()
            unitCostUsd = p.unitCostUsd.toString()
            status = p.status
            purchaseDate = p.purchaseDate
            shippingMethod = p.shippingMethod
            weightKg = p.weightKg?.toString() ?: ""
            lengthCm = p.lengthCm?.toString() ?: ""
            widthCm = p.widthCm?.toString() ?: ""
            heightCm = p.heightCm?.toString() ?: ""
            unitShippingCostUsd = p.unitShippingCostUsd.toString()
            otherExpensesUsd = p.otherExpensesUsd?.toString() ?: ""
            potentialSellingPriceUsd = p.potentialSellingPriceUsd.toString()
            trackingNumber = p.trackingNumber ?: ""
            shippingAgency = p.shippingAgency ?: ""
            photoBase64 = p.photoBase64
        } else {
            // Set defaults for new
            name = ""
            category = ""
            description = ""
            quantity = "1"
            unitCostUsd = ""
            status = "Planificado"
            purchaseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            shippingMethod = "Por Peso"
            weightKg = ""
            lengthCm = ""
            widthCm = ""
            heightCm = ""
            unitShippingCostUsd = ""
            otherExpensesUsd = ""
            potentialSellingPriceUsd = ""
            trackingNumber = ""
            shippingAgency = ""
            photoBase64 = null
        }
    }

    // Image Picker launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val compressed = ImageCompressor.uriToBase64(context, uri)
                if (compressed != null) {
                    photoBase64 = compressed
                }
            }
        }
    }

    // Numerical helpers
    val qtyInt = quantity.toIntOrNull() ?: 1
    val costDouble = unitCostUsd.toDoubleOrNull() ?: 0.0
    val shippingDouble = unitShippingCostUsd.toDoubleOrNull() ?: 0.0
    val otherDouble = otherExpensesUsd.toDoubleOrNull() ?: 0.0
    val sellingPriceDouble = potentialSellingPriceUsd.toDoubleOrNull() ?: 0.0

    // Calculations
    val totalProductCost = qtyInt * costDouble
    val totalShippingCost = qtyInt * shippingDouble
    val landedCostTotal = totalProductCost + totalShippingCost + otherDouble
    val unitLandedCost = if (qtyInt > 0) landedCostTotal / qtyInt else 0.0
    
    val unitProfit = sellingPriceDouble - unitLandedCost
    val lotProfit = (sellingPriceDouble * qtyInt) - landedCostTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Form Title top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("history") },
                    modifier = Modifier.testTag("form_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingPurchase != null) "Editar Compra" else "Registrar Compra",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            AppLogo(size = 36.dp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Safe bitmap decoding outside Try-Catch of Composable
            val computedBitmap = remember(photoBase64) {
                if (photoBase64 != null) {
                    try {
                        val decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            // Photo picker widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable { pickerLauncher.launch("image/*") }
                    .testTag("form_photo_box"),
                contentAlignment = Alignment.Center
            ) {
                if (computedBitmap != null) {
                    Image(
                        bitmap = computedBitmap.asImageBitmap(),
                        contentDescription = "Foto del producto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PhotoPlaceholder()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seccion 1: PRODUCTO
            SectionHeader(title = "PRODUCTO")
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto (requerido)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_category"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_desc"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seccion 2: COMPRA
            SectionHeader(title = "INFORMACIÓN DE COMPRA")

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_qty"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = unitCostUsd,
                onValueChange = { unitCostUsd = it },
                label = { Text("Costo Unitario (USD) - sin envío incluido") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_unit_cost"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Estado dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado de la Compra") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("form_dropdown_status"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    listOf("Planificado", "En Tránsito", "En Cuba", "Vendido").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = purchaseDate,
                onValueChange = { purchaseDate = it },
                label = { Text("Fecha de Compra (AAAA-MM-DD)") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_input_date"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seccion 3: ENVÍO
            SectionHeader(title = "ENVÍO")

            ExposedDropdownMenuBox(
                expanded = methodExpanded,
                onExpandedChange = { methodExpanded = !methodExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = shippingMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Método de Envío") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("form_dropdown_shipping"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = methodExpanded,
                    onDismissRequest = { methodExpanded = false }
                ) {
                    listOf("Por Peso", "Por Dimensiones", "Mayor").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                shippingMethod = option
                                methodExpanded = false
                            }
                        )
                    }
                }
            }

            // Conditional shipping inputs
            if (shippingMethod == "Por Peso") {
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Peso Unitario en kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("form_input_weight"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            } else if (shippingMethod == "Por Dimensiones") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = lengthCm,
                            onValueChange = { lengthCm = it },
                            label = { Text("Largo (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = widthCm,
                            onValueChange = { widthCm = it },
                            label = { Text("Ancho (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = heightCm,
                            onValueChange = { heightCm = it },
                            label = { Text("Alto (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = trackingNumber,
                onValueChange = { trackingNumber = it },
                label = { Text("Número de Seguimiento (Tracking #)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_tracking"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = shippingAgency,
                onValueChange = { shippingAgency = it },
                label = { Text("Agencia de Carga (ej: Aerovaradero)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_agency"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = unitShippingCostUsd,
                onValueChange = { unitShippingCostUsd = it },
                label = { Text("Costo de Envío (USD) - unitario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_shipping_cost"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = otherExpensesUsd,
                onValueChange = { otherExpensesUsd = it },
                label = { Text("Otros Gastos Totales (USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("form_input_other_expenses"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = potentialSellingPriceUsd,
                onValueChange = { potentialSellingPriceUsd = it },
                label = { Text("Precio Potencial de Venta (USD) - por unidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_input_selling_price"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Seccion 4: CÁLCULOS EN TIEMPO REAL
            SectionHeader(title = "CÁLCULOS AUTOMÁTICOS")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    CalculationRow(label = "Costo Total Productos:", value = "$${String.format("%.2f", totalProductCost)}")
                    CalculationRow(label = "Costo Total Envío:", value = "$${String.format("%.2f", totalShippingCost)}")
                    CalculationRow(label = "Inversión Final:", value = "$${String.format("%.2f", landedCostTotal)}", isBold = true)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val marginColor = if (unitProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    CalculationRow(
                        label = "Ganancia Proyectada Unit.:",
                        value = "$${String.format("%.2f", unitProfit)}",
                        valueColor = marginColor,
                        isBold = true
                    )
                    CalculationRow(
                        label = "Ganancia Proyectada Lote:",
                        value = "$${String.format("%.2f", lotProfit)}",
                        valueColor = marginColor,
                        isBold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.trim().length < 3) {
                        return@Button
                    }
                    viewModel.savePurchase(
                        Purchase(
                            id = editingPurchase?.id ?: 0,
                            photoBase64 = photoBase64,
                            name = name,
                            category = category.ifBlank { null },
                            description = description.ifBlank { null },
                            quantity = qtyInt,
                            unitCostUsd = costDouble,
                            status = status,
                            purchaseDate = purchaseDate,
                            shippingMethod = shippingMethod,
                            weightKg = weightKg.toDoubleOrNull(),
                            lengthCm = lengthCm.toDoubleOrNull(),
                            widthCm = widthCm.toDoubleOrNull(),
                            heightCm = heightCm.toDoubleOrNull(),
                            unitShippingCostUsd = shippingDouble,
                            otherExpensesUsd = otherExpensesUsd.toDoubleOrNull(),
                            potentialSellingPriceUsd = sellingPriceDouble,
                            trackingNumber = trackingNumber.ifBlank { null },
                            shippingAgency = shippingAgency.ifBlank { null }
                        )
                    )
                },
                enabled = name.trim().length >= 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("form_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (editingPurchase != null) "Actualizar Compra" else "Registrar Compra",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (name.trim().length >= 3) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PhotoPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Añadir Foto",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "(Cámara o Galería - Auto-compresión)",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun CalculationRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
