package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Purchase
import com.example.data.Sale
import com.example.data.Customer
import com.example.ui.MainViewModel
import com.example.ui.components.AppLogo
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchaseListScreen(viewModel: MainViewModel) {
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var activeTab by remember { mutableStateOf("purchases") } // "purchases", "sales", "customers"
    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }

    // Dialogs state
    var purchaseToDelete by remember { mutableStateOf<Purchase?>(null) }
    var purchaseToSell by remember { mutableStateOf<Purchase?>(null) }
    var showCustomerDialog by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Historial General",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Administra tus inventarios y ventas realizadas",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                AppLogo(size = 44.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Tab (Compras | Ventas)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeTab == "purchases") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "purchases" }
                        .padding(vertical = 12.dp)
                        .testTag("tab_purchases"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Compras",
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "purchases") Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeTab == "sales") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "sales" }
                        .padding(vertical = 12.dp)
                        .testTag("tab_sales"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ventas",
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "sales") Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeTab == "customers") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "customers" }
                        .padding(vertical = 12.dp)
                        .testTag("tab_customers"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clientes",
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "customers") Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (activeTab == "purchases") {
            // Search + Filters for purchases
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Buscar compra por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filters LazyRow
                val filters = listOf("Todos", "Planificado", "En Tránsito", "En Cuba", "Vendido")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { f ->
                        val selected = selectedStatusFilter == f
                        val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val txtColor = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { selectedStatusFilter = f }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("filter_$f")
                        ) {
                            Text(
                                text = f,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = txtColor
                            )
                        }
                    }
                }
            }

            // Filter logic
            val filteredPurchases = purchases.filter { p ->
                val matchesText = p.name.contains(searchText, ignoreCase = true)
                val matchesStatus = selectedStatusFilter == "Todos" || p.status.equals(selectedStatusFilter, ignoreCase = true)
                matchesText && matchesStatus
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredPurchases.isEmpty()) {
                EmptySearchHistoryState(onCta = { viewModel.navigateTo("purchase_form") })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPurchases) { purchase ->
                        val salesForThis = sales.filter { it.purchaseId == purchase.id }
                        val qtySold = salesForThis.sumOf { it.quantitySold }
                        val stockRemaining = purchase.quantity - qtySold

                        PurchaseItemCard(
                            purchase = purchase,
                            stockRemaining = stockRemaining,
                            onEdit = {
                                viewModel.setEditingPurchase(purchase)
                                viewModel.navigateTo("purchase_form")
                            },
                            onDuplicate = { viewModel.duplicatePurchase(purchase) },
                            onDelete = { purchaseToDelete = purchase },
                            onSell = { purchaseToSell = purchase }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        } else if (activeTab == "sales") {
            // Sales Report History
            if (sales.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Sell, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay ventas registradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales) { sale ->
                        val p = purchases.firstOrNull { it.id == sale.purchaseId }
                        val customer = customers.firstOrNull { it.id == sale.customerId }
                        SaleReportCard(
                            sale = sale, 
                            purchase = p, 
                            customerName = customer?.name,
                            onDelete = { viewModel.deleteSale(sale.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        } else {
            // Customers Tab
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Button(
                    onClick = { showCustomerDialog = Customer(name = "") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Cliente", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                if (customers.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No hay clientes registrados", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(customers) { customer ->
                            val customerSales = sales.filter { it.customerId == customer.id }
                            val totalDebt = customerSales.sumOf { it.totalAmountUsd - it.amountPaidUsd }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        if (!customer.phone.isNullOrBlank()) {
                                            Text(customer.phone, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        if (totalDebt > 0) {
                                            Text(
                                                "Deuda: $${String.format("%.2f", totalDebt)}",
                                                color = Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        } else {
                                            Text("Sin deudas", color = Color(0xFF10B981), fontSize = 12.sp)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = { showCustomerDialog = customer }) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { customerToDelete = customer }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar Compra?") },
            text = { Text("Esta acción borrará irreversiblemente la compra de \"${purchase.name}\" junto con todas sus ventas asociadas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePurchase(purchase.id)
                        purchaseToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar Cliente?") },
            text = { Text("¿Estás seguro de eliminar a ${customer.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCustomer(customer); customerToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    showCustomerDialog?.let { customer ->
        var name by remember(customer) { mutableStateOf(customer.name) }
        var phone by remember(customer) { mutableStateOf(customer.phone ?: "") }
        var notes by remember(customer) { mutableStateOf(customer.notes ?: "") }

        AlertDialog(
            onDismissRequest = { showCustomerDialog = null },
            title = { Text(if (customer.id == 0) "Añadir Cliente" else "Editar Cliente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomer(customer.copy(name = name, phone = phone, notes = notes))
                        showCustomerDialog = null
                    },
                    enabled = name.isNotBlank()
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerDialog = null }) { Text("Cancelar") }
            }
        )
    }

    // Record Sale Dialog (UC-04)
    purchaseToSell?.let { p ->
        val salesForThis = sales.filter { it.purchaseId == p.id }
        val qtySold = salesForThis.sumOf { it.quantitySold }
        val stockRemaining = p.quantity - qtySold

        var sellQtyStr by remember(p) { mutableStateOf("1") }
        var sellPriceStr by remember(p) { mutableStateOf(p.potentialSellingPriceUsd.toString()) }
        var sellDate by remember(p) { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var selectedCustomer by remember(p) { mutableStateOf<Int?>(null) }
        var amountPaidStr by remember(p) { mutableStateOf("") }
        var customerDropdownExpanded by remember { mutableStateOf(false) }

        val sellQty = sellQtyStr.toIntOrNull() ?: 0
        val sellPrice = sellPriceStr.toDoubleOrNull() ?: 0.0
        val totalIncome = sellQty * sellPrice
        val amountPaid = amountPaidStr.toDoubleOrNull() ?: totalIncome

        AlertDialog(
            onDismissRequest = { purchaseToSell = null },
            icon = { Icon(Icons.Default.Sell, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Registrar Venta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${p.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Stock Disponible: $stockRemaining unidades", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    
                    // Customer Selector
                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                    ) {
                        val currentCustomer = customers.find { it.id == selectedCustomer }
                        OutlinedTextField(
                            value = currentCustomer?.name ?: "Venta al Contado (Sin Cliente)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Venta al Contado (Sin Cliente)") },
                                onClick = { selectedCustomer = null; customerDropdownExpanded = false }
                            )
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.name) },
                                    onClick = { selectedCustomer = customer.id; customerDropdownExpanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = sellQtyStr,
                        onValueChange = { sellQtyStr = it },
                        label = { Text("Cantidad a Vender") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("sell_input_qty"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text("Precio de Venta Unit. (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("sell_input_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    if (selectedCustomer != null) {
                        OutlinedTextField(
                            value = amountPaidStr,
                            onValueChange = { amountPaidStr = it },
                            label = { Text("Monto Pagado Hoy (USD)") },
                            placeholder = { Text("Dejar vacío si pagó completo: $${String.format("%.2f", totalIncome)}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("sell_input_paid"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = sellDate,
                        onValueChange = { sellDate = it },
                        label = { Text("Fecha de Venta") },
                        modifier = Modifier.fillMaxWidth().testTag("sell_input_date"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ingreso Total:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format("%.2f", totalIncome)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (selectedCustomer != null && amountPaid < totalIncome) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deuda Generada:", fontSize = 12.sp, color = Color(0xFFEF4444))
                            Text("$${String.format("%.2f", totalIncome - amountPaid)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = sellQty > 0 && sellQty <= stockRemaining && sellPrice > 0,
                    onClick = {
                        viewModel.recordSale(
                            purchaseId = p.id,
                            quantitySold = sellQty,
                            pricePerUnitUsd = sellPrice,
                            date = sellDate,
                            customerId = selectedCustomer,
                            amountPaidUsd = if (amountPaidStr.isBlank()) null else amountPaid
                        )
                        purchaseToSell = null
                    }
                ) {
                    Text("Confirmar Venta")
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToSell = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: Purchase,
    stockRemaining: Int,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onSell: () -> Unit
) {
    val totalCost = (purchase.quantity * purchase.unitCostUsd) + (purchase.quantity * purchase.unitShippingCostUsd) + (purchase.otherExpensesUsd ?: 0.0)
    
    val decodedBitmap = remember(purchase.photoBase64) {
        if (purchase.photoBase64 != null) {
            try {
                val decoded = Base64.decode(purchase.photoBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row upper info: Photo + status
            Row(modifier = Modifier.fillMaxWidth()) {
                // Miniature photo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF262626)),
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(
                            bitmap = decodedBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name, Qty and status badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = purchase.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Status badge
                        StatusBadge(status = purchase.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = purchase.category ?: "Sin Categoría",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = purchase.purchaseDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Stock: $stockRemaining / ${purchase.quantity} u.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stockRemaining > 0) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 mini-cards of costs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modifierCell = Modifier.weight(1f)
                
                // Weight/Dim cell
                MiniCell(
                    modifier = modifierCell,
                    label = "Envío",
                    value = when (purchase.shippingMethod) {
                        "Por Peso" -> "${purchase.weightKg ?: 0.0} kg"
                        "Por Dimensiones" -> "${purchase.lengthCm ?: 0.0}x${purchase.widthCm ?: 0.0} cm"
                        else -> "Mayor"
                    }
                )

                // Costo Unitario
                MiniCell(
                    modifier = modifierCell,
                    label = "Costo Un.",
                    value = "$${String.format("%.1f", purchase.unitCostUsd)}"
                )

                // Costo Total
                MiniCell(
                    modifier = modifierCell,
                    label = "Costo Tot.",
                    value = "$${String.format("%.1f", totalCost)}"
                )

                // Precio Potencial
                MiniCell(
                    modifier = modifierCell,
                    label = "P. Venta",
                    value = "$${String.format("%.1f", purchase.potentialSellingPriceUsd)}"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary actions: Edit, Duplicate, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }

                // Main CTA: Vender
                if (stockRemaining > 0) {
                    Button(
                        onClick = onSell,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Sell, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vender", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Vendido",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, txtColor) = when (status) {
        "Planificado" -> Color(0xFF27272A) to Color(0xFFA1A1AA)
        "En Tránsito" -> Color(0xFF1E3A8A) to Color(0xFF60A5FA)
        "En Cuba" -> Color(0xFF78350F) to Color(0xFFFBBF24)
        "Vendido" -> Color(0xFF064E3B) to Color(0xFF34D399)
        else -> Color.DarkGray to Color.LightGray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = txtColor
        )
    }
}

@Composable
fun MiniCell(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .background(Color(0xFF262626), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SaleReportCard(
    sale: Sale,
    purchase: Purchase?,
    customerName: String?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = purchase?.name ?: "Producto Eliminado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                if (customerName != null) {
                    Text(
                        text = "Cliente: $customerName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cant: ${sale.quantitySold} u.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "P. Unit: $${String.format("%.1f", sale.pricePerUnitUsd)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                val debt = sale.totalAmountUsd - sale.amountPaidUsd
                if (debt > 0) {
                    Text(
                        text = "Deuda pendiente: $${String.format("%.2f", debt)}",
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Pagado completo",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Fecha: ${sale.saleDate}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val revenue = sale.quantitySold * sale.pricePerUnitUsd
                Text(
                    text = "+$${String.format("%.1f", revenue)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF10B981)
                )

                if (purchase != null) {
                    val unitLandedCost = purchase.unitCostUsd + purchase.unitShippingCostUsd + ((purchase.otherExpensesUsd ?: 0.0) / purchase.quantity)
                    val margin = sale.quantitySold * (sale.pricePerUnitUsd - unitLandedCost)
                    Text(
                        text = "Ganancia: $${String.format("%.1f", margin)}",
                        fontSize = 11.sp,
                        color = if (margin >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Venta", tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun EmptySearchHistoryState(onCta: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No se encontraron compras",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCta,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Registrar Nueva Compra", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
