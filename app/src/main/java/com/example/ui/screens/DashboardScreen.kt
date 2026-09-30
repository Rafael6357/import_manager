package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.Purchase
import com.example.data.Sale
import com.example.ui.MainViewModel
import com.example.ui.components.AppLogo
import com.example.ui.components.ScatterChart
import com.example.ui.components.SimpleBarChart
import com.example.ui.components.SimpleLineChart
import com.example.ui.components.SimplePieChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var lineFilter by remember { mutableStateOf("6m") } // "6m" or "1y"

    if (purchases.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo(size = 120.dp)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No hay datos suficientes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Crea tu primera compra para comenzar a gestionar tus importaciones a Cuba offline.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.navigateTo("purchase_form") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_first_purchase")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear primera compra")
                }
            }
        }
        return
    }

    // Mathematical computations of Cuban Landed Costs & Sales profit offline
    val activePurchases = purchases.filter { it.status != "Vendido" }
    
    // Inversión Activa
    val activeInvestment = activePurchases.sumOf { p ->
        val itemCost = p.quantity * p.unitCostUsd
        val shippingCost = p.quantity * p.unitShippingCostUsd
        val extra = p.otherExpensesUsd ?: 0.0
        itemCost + shippingCost + extra
    }

    // Inversión Histórica Total
    val totalHistoricalInvestment = purchases.sumOf { p ->
        val itemCost = p.quantity * p.unitCostUsd
        val shippingCost = p.quantity * p.unitShippingCostUsd
        val extra = p.otherExpensesUsd ?: 0.0
        itemCost + shippingCost + extra
    }

    // Ganancia Potencial Aproximada (Active items potential revenue - active landed cost)
    val potentialProfitApprox = activePurchases.sumOf { p ->
        val totalRevenue = p.quantity * p.potentialSellingPriceUsd
        val landedCost = (p.quantity * p.unitCostUsd) + (p.quantity * p.unitShippingCostUsd) + (p.otherExpensesUsd ?: 0.0)
        (totalRevenue - landedCost).coerceAtLeast(0.0)
    }

    // Ganancia Confirmada (Sales realized)
    val confirmedProfit = sales.sumOf { s ->
        val parentPurchase = purchases.firstOrNull { it.id == s.purchaseId }
        if (parentPurchase != null) {
            val unitLandedCost = parentPurchase.unitCostUsd + parentPurchase.unitShippingCostUsd + ((parentPurchase.otherExpensesUsd ?: 0.0) / parentPurchase.quantity)
            s.quantitySold * (s.pricePerUnitUsd - unitLandedCost)
        } else {
            0.0
        }
    }

    // Capital en la calle (Cuentas por cobrar)
    val accountsReceivable = sales.sumOf { it.totalAmountUsd - it.amountPaidUsd }

    // ROI Histórico (Confirmado)
    val confirmedCost = sales.sumOf { s ->
        val parentPurchase = purchases.firstOrNull { it.id == s.purchaseId }
        if (parentPurchase != null) {
            val unitLandedCost = parentPurchase.unitCostUsd + parentPurchase.unitShippingCostUsd + ((parentPurchase.otherExpensesUsd ?: 0.0) / parentPurchase.quantity)
            s.quantitySold * unitLandedCost
        } else {
            0.0
        }
    }
    val confirmedRoi = if (confirmedCost > 0) (confirmedProfit / confirmedCost) * 100 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header (Modern, minimal, like Revolut/Trade Republic)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ImportManager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            AppLogo(size = 36.dp)
        }

        // Summary Cards Grid (Elegant 2x2 Grid)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            val totalSalesValue = sales.sumOf { s -> s.quantitySold * s.pricePerUnitUsd }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Inversión Activa",
                    value = "$${String.format(Locale.US, "%,.2f", activeInvestment)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    trendText = "+2.4%",
                    isPositive = true,
                    testTag = "metric_active_inv",
                    tooltipText = "Costo total de importaciones activas en inventario (compras, envío y gastos extra).",
                    modifier = Modifier.weight(1f).height(125.dp)
                )
                MetricCard(
                    title = "Ganancia Proyectada",
                    value = "$${String.format(Locale.US, "%,.2f", potentialProfitApprox)}",
                    icon = Icons.Default.ShowChart,
                    trendText = "+8.1%",
                    isPositive = true,
                    testTag = "metric_potential_gain",
                    tooltipText = "Ganancia estimada calculada sobre precios proyectados menos costos de importación.",
                    modifier = Modifier.weight(1f).height(125.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Capital en la Calle",
                    value = "$${String.format(Locale.US, "%,.2f", accountsReceivable)}",
                    icon = Icons.Default.TrendingDown,
                    trendText = "Deudas",
                    isPositive = false,
                    testTag = "metric_debts",
                    tooltipText = "Total de dinero pendiente por cobrar de ventas realizadas a plazos (fiados).",
                    modifier = Modifier.weight(1f).height(125.dp)
                )
                MetricCard(
                    title = "ROI Histórico",
                    value = "${String.format(Locale.US, "%.1f", confirmedRoi)}%",
                    icon = Icons.Default.BarChart,
                    trendText = "Rendimiento",
                    isPositive = true,
                    testTag = "metric_roi",
                    tooltipText = "Retorno de Inversión real basado en las ventas ya cobradas y sus costos asociados.",
                    modifier = Modifier.weight(1f).height(125.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Gráfico de torta: Estado de Compras
        ChartContainer(title = "Estado de Compras", icon = Icons.Default.PieChart) {
            val statesCount = purchases.groupBy { it.status }
                .map { (status, list) -> status to list.size.toFloat() }
            val colorMap = mapOf(
                "Planificado" to Color(0xFF71717A),
                "En Tránsito" to Color(0xFF3B82F6),
                "En Cuba" to Color(0xFFF59E0B),
                "Vendido" to Color(0xFF10B981)
            )
            SimplePieChart(data = statesCount, colors = colorMap)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Gráfico de línea: Ganancia en el tiempo (ventas realizadas agrupadas por mes)
        ChartContainer(
            title = "Ganancia en el Tiempo",
            icon = Icons.Default.TrendingUp,
            action = {
                Row(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    listOf("6m" to "6 Meses", "1y" to "1 Año").forEach { (id, label) ->
                        val selected = lineFilter == id
                        Box(
                            modifier = Modifier
                                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { lineFilter = id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) {
            // Aggregate sales based on filters
            val lineData = getAggregatedSalesData(sales, purchases, lineFilter)
            SimpleLineChart(data = lineData, color = Color(0xFF10B981))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top 5 productos más rentables
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Top Productos Más Rentables",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                val sortedRentables = purchases.map { p ->
                    val landed = (p.quantity * p.unitCostUsd) + (p.quantity * p.unitShippingCostUsd) + (p.otherExpensesUsd ?: 0.0)
                    val revenue = p.quantity * p.potentialSellingPriceUsd
                    val profit = revenue - landed
                    val profitMarginPercent = if (landed > 0) (profit / landed) * 100f else 0.0
                    Triple(p.name, profit, profitMarginPercent)
                }.sortedByDescending { it.second }.take(5)

                sortedRentables.forEachIndexed { index, triple ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = triple.first,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+$${String.format("%.2f", triple.second)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "${String.format("%.1f", triple.third)}% marg.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    trendText: String,
    isPositive: Boolean,
    testTag: String,
    tooltipText: String,
    modifier: Modifier = Modifier
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clickable { showTooltip = !showTooltip },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = trendText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showTooltip) {
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, -115),
                onDismissRequest = { showTooltip = false },
                properties = PopupProperties(focusable = true)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = tooltipText,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChartContainer(
    title: String,
    icon: ImageVector,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

private fun getAggregatedSalesData(
    sales: List<Sale>,
    purchases: List<Purchase>,
    filter: String
): List<Pair<String, Float>> {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMM", Locale("es", "CU"))
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)

    val limitMonths = if (filter == "6m") 6 else 12
    val resultList = ArrayList<Pair<String, Float>>()

    // Generate last N months placeholders
    for (i in (limitMonths - 1) downTo 0) {
        val loopCal = Calendar.getInstance()
        loopCal.add(Calendar.MONTH, -i)
        val key = sdf.format(loopCal.time)
        resultList.add(Pair(key, 0f))
    }

    val parsingSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    for (s in sales) {
        val parentPurchase = purchases.firstOrNull { it.id == s.purchaseId } ?: continue
        val unitLandedCost = parentPurchase.unitCostUsd + parentPurchase.unitShippingCostUsd + ((parentPurchase.otherExpensesUsd ?: 0.0) / parentPurchase.quantity)
        val profit = s.quantitySold * (s.pricePerUnitUsd - unitLandedCost)

        try {
            val date = parsingSdf.parse(s.saleDate) ?: continue
            val sCal = Calendar.getInstance()
            sCal.time = date
            val monthLabel = sdf.format(date)

            // Update matching month placeholder
            val index = resultList.indexOfFirst { it.first.equals(monthLabel, ignoreCase = true) }
            if (index != -1) {
                val currentVal = resultList[index].second
                resultList[index] = Pair(resultList[index].first, currentVal + profit.toFloat())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return resultList
}
