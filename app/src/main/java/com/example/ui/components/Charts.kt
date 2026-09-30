package com.example.ui.components

import android.graphics.Paint
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.height(200.dp).fillMaxWidth()
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No hay datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1f)

    Column(modifier = Modifier.padding(8.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val spacing = 24f
            val barCount = data.size
            val barWidth = (width - (spacing * (barCount + 1))) / barCount

            for (i in data.indices) {
                val item = data[i]
                val barHeight = (item.second / maxValue) * (height - 60f)
                val left = spacing + i * (barWidth + spacing)
                val top = height - barHeight - 40f

                // Draw bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Draw label abbreviated
                val label = if (item.first.length > 8) item.first.take(6) + ".." else item.first
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    left + barWidth / 2f,
                    height - 10f,
                    Paint().apply {
                        this.color = Color.White.toArgb()
                        this.textSize = 28f
                        this.textAlign = Paint.Align.CENTER
                    }
                )

                // Draw value above bar
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.0f", item.second),
                    left + barWidth / 2f,
                    top - 10f,
                    Paint().apply {
                        this.color = color.toArgb()
                        this.textSize = 26f
                        this.textAlign = Paint.Align.CENTER
                        this.isFakeBoldText = true
                    }
                )
            }
        }
    }
}

@Composable
fun SimplePieChart(
    data: List<Pair<String, Float>>,
    colors: Map<String, Color>,
    modifier: Modifier = Modifier.size(140.dp)
) {
    val total = data.sumOf { it.second.toDouble() }.toFloat()
    if (total <= 0f) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Cero datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = modifier) {
                var startAngle = -90f
                val strokeWidth = 36f
                for (item in data) {
                    val sweepAngle = (item.second / total) * 360f
                    val color = colors[item.first] ?: Color.Gray
                    
                    // Draw inside a smaller rect to avoid edge clipping
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal legends list, centered and clean
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val nonZeroItems = data.filter { it.second > 0f }
            nonZeroItems.forEachIndexed { idx, item ->
                val color = colors[item.first] ?: Color.Gray
                val percentage = (item.second / total) * 100f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.first} (${String.format(Locale.US, "%.0f", percentage)}%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
fun ScatterChart(
    points: List<Pair<Float, Float>>, // List of Pair(Cost, Profit)
    modifier: Modifier = Modifier.height(180.dp).fillMaxWidth()
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No hay suficientes datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxCost = points.maxOf { it.first }.coerceAtLeast(10f)
    val maxProfit = points.maxOf { it.second }.coerceAtLeast(10f)
    val minProfit = points.minOf { it.second }.coerceAtMost(0f)
    val profitRange = (maxProfit - minProfit).coerceAtLeast(1f)
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        val width = size.width
        val height = size.height

        // Draw Axes
        drawLine(
            color = outlineColor,
            start = Offset(40f, 0f),
            end = Offset(40f, height - 30f),
            strokeWidth = 3f
        )
        drawLine(
            color = outlineColor,
            start = Offset(40f, height - 30f),
            end = Offset(width, height - 30f),
            strokeWidth = 3f
        )

        // Plot dots
        for (pt in points) {
            val cx = 40f + (pt.first / maxCost) * (width - 80f)
            val cy = (height - 30f) - ((pt.second - minProfit) / profitRange) * (height - 60f)
            
            // Draw visual drop line
            drawLine(
                color = Color(0xFF262626),
                start = Offset(cx, cy),
                end = Offset(cx, height - 30f),
                strokeWidth = 1f
            )

            drawCircle(
                color = if (pt.second >= 0f) Color(0xFF10B981) else Color(0xFFEF4444),
                radius = 12f,
                center = Offset(cx, cy)
            )
        }

        // Draw axis text
        drawContext.canvas.nativeCanvas.drawText(
            "Costo",
            width - 50f,
            height - 5f,
            Paint().apply {
                this.color = Color.Gray.toArgb()
                this.textSize = 24f
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            "Ganancia",
            50f,
            25f,
            Paint().apply {
                this.color = Color.Gray.toArgb()
                this.textSize = 24f
            }
        )
    }
}

@Composable
fun SimpleLineChart(
    data: List<Pair<String, Float>>, // List of Month/Date -> Value
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.height(180.dp).fillMaxWidth()
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No hay suficientes ventas en el tiempo", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1f)
    val minValue = data.minOf { it.second }.coerceAtMost(0f)
    val valueRange = (maxValue - minValue).coerceAtLeast(1f)

    Canvas(modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        val gridLinesCount = 5
        for (g in 0 until gridLinesCount) {
            val y = (height - 40f) / (gridLinesCount - 1) * g + 10f
            drawLine(
                color = Color(0xFF222424), // Subtle dark line matching outline-variant
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.5f
            )
        }

        val pointCount = data.size
        val xStep = if (pointCount > 1) width / (pointCount - 1) else width

        val points = data.indices.map { i ->
            val x = i * xStep
            val y = height - ((data[i].second - minValue) / valueRange) * height
            Offset(x, y)
        }

        // Fill path
        if (points.isNotEmpty()) {
            val fillPath = Path().apply {
                moveTo(0f, height)
                for (i in points.indices) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(width, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
                )
            )

            // Stroke path
            val strokePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = strokePath,
                color = color,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Dots
            for (pt in points) {
                drawCircle(
                    color = color,
                    radius = 8f,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = pt
                )
            }
        }

        // Draw x-axis labels
        for (i in data.indices) {
            val label = data[i].first
            val x = i * xStep
            val abbrev = if (label.length > 5) label.take(4) else label
            drawContext.canvas.nativeCanvas.drawText(
                abbrev,
                x.coerceIn(30f, width - 30f),
                height - 4f,
                Paint().apply {
                    this.color = Color.Gray.toArgb()
                    this.textSize = 24f
                    this.textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}
