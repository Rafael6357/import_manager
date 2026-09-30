package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Canvas(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
    ) {
        val width = this.size.width
        val scale = width / 512f

        // <rect x="0" y="0" width="512" height="512" rx="100" fill="#0A0B0D"/>
        drawRoundRect(
            color = Color(0xFF0A0B0D),
            topLeft = Offset(0f, 0f),
            size = Size(512f * scale, 512f * scale),
            cornerRadius = CornerRadius(100f * scale, 100f * scale)
        )

        // <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        // <stop offset="0%" stop-color="#38BDF8"/>
        // <stop offset="55%" stop-color="#10B981"/>
        // <stop offset="100%" stop-color="#0F766E"/>
        val ringBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF38BDF8),
                Color(0xFF10B981),
                Color(0xFF0F766E)
            ),
            start = Offset(0f, 0f),
            end = Offset(512f * scale, 512f * scale)
        )

        // <circle cx="256" cy="256" r="188" fill="url(#ringGrad)"/>
        drawCircle(
            brush = ringBrush,
            radius = 188f * scale,
            center = Offset(256f * scale, 256f * scale)
        )

        // <linearGradient id="innerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        // <stop offset="0%" stop-color="#111827"/>
        // <stop offset="100%" stop-color="#020304"/>
        val innerBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF111827),
                Color(0xFF020304)
            ),
            start = Offset(0f, 0f),
            end = Offset(512f * scale, 512f * scale)
        )

        // <circle cx="256" cy="256" r="152" fill="url(#innerGrad)"/>
        drawCircle(
            brush = innerBrush,
            radius = 152f * scale,
            center = Offset(256f * scale, 256f * scale)
        )

        // <rect x="164" y="272" width="88" height="66" rx="8" fill="#94A3B8"/>
        drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(164f * scale, 272f * scale),
            size = Size(88f * scale, 66f * scale),
            cornerRadius = CornerRadius(8f * scale, 8f * scale)
        )

        // <line x1="164" y1="300" x2="252" y2="300" stroke="#0A0B0D" stroke-width="4"/>
        drawLine(
            color = Color(0xFF0A0B0D),
            start = Offset(164f * scale, 300f * scale),
            end = Offset(252f * scale, 300f * scale),
            strokeWidth = 4f * scale
        )

        // <line x1="208" y1="272" x2="208" y2="338" stroke="#0A0B0D" stroke-width="4"/>
        drawLine(
            color = Color(0xFF0A0B0D),
            start = Offset(208f * scale, 272f * scale),
            end = Offset(208f * scale, 338f * scale),
            strokeWidth = 4f * scale
        )

        // <rect x="266" y="308" width="24" height="30" rx="4" fill="#34D399"/>
        drawRoundRect(
            color = Color(0xFF34D399),
            topLeft = Offset(266f * scale, 308f * scale),
            size = Size(24f * scale, 30f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )

        // <rect x="300" y="276" width="24" height="62" rx="4" fill="#34D399"/>
        drawRoundRect(
            color = Color(0xFF34D399),
            topLeft = Offset(300f * scale, 276f * scale),
            size = Size(24f * scale, 62f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )

        // <rect x="334" y="234" width="24" height="104" rx="4" fill="#6EE7B7"/>
        drawRoundRect(
            color = Color(0xFF6EE7B7),
            topLeft = Offset(334f * scale, 234f * scale),
            size = Size(24f * scale, 104f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )
    }
}
