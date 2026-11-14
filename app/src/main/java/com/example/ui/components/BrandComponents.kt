package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite
import java.text.NumberFormat
import java.util.Locale

val NovaBrandGradient = Brush.linearGradient(
    colors = listOf(NovaPink, NovaTurquoise, NovaCyan)
)

val NovaPinkCyanGradient = Brush.horizontalGradient(
    colors = listOf(NovaPink, Color(0xFFFF389C), NovaTurquoise, NovaCyan)
)

val NovaSoftBannerGradient = Brush.linearGradient(
    colors = listOf(NovaPinkSoft, Color(0xFFF3FAFF), NovaCyanSoft)
)

fun formatTaka(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    return "৳${formatter.format(amount.toLong())}"
}

fun formatCountdown(endMillis: Long, nowMillis: Long): Triple<String, String, String> {
    val diffSec = ((endMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    val hrs = diffSec / 3600L
    val mins = (diffSec % 3600L) / 60L
    val secs = diffSec % 60L
    return Triple(
        hrs.toString().padStart(2, '0'),
        mins.toString().padStart(2, '0'),
        secs.toString().padStart(2, '0')
    )
}

@Composable
fun ShopNovaLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NovaNavy),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val w = size.width
                val h = size.height
                // Stylized shopping bag body
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(NovaPink, NovaCyan)),
                    topLeft = Offset(w * 0.16f, h * 0.30f),
                    size = Size(w * 0.68f, h * 0.58f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 3.2f)
                )
                // Bag handle arc
                drawArc(
                    brush = Brush.horizontalGradient(listOf(NovaPink, NovaCyan)),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.32f, h * 0.10f),
                    size = Size(w * 0.36f, h * 0.36f),
                    style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                )
                // 4-point Nova Star inside
                val cx = w * 0.50f
                val cy = h * 0.59f
                val rOuter = w * 0.19f
                val rInner = w * 0.06f
                val star = Path().apply {
                    moveTo(cx, cy - rOuter)
                    lineTo(cx + rInner, cy - rInner)
                    lineTo(cx + rOuter, cy)
                    lineTo(cx + rInner, cy + rInner)
                    lineTo(cx, cy + rOuter)
                    lineTo(cx - rInner, cy + rInner)
                    lineTo(cx - rOuter, cy)
                    lineTo(cx - rInner, cy - rInner)
                    close()
                }
                drawPath(
                    path = star,
                    brush = Brush.linearGradient(listOf(NovaCyan, NovaPink))
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = NovaNavy, fontWeight = FontWeight.ExtraBold)) {
                            append("Shop")
                        }
                        withStyle(SpanStyle(color = NovaPink, fontWeight = FontWeight.ExtraBold)) {
                            append("Nova")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (compact) 18.sp else 20.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.horizontalGradient(listOf(NovaPink, NovaTurquoise)))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "BD",
                        color = NovaWhite,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
            if (!compact) {
                Text(
                    text = "Smart Shopping • Bangladesh",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CountdownPillBox(hours: String, minutes: String, seconds: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeUnitChip(hours)
        Text(":", color = NovaWhite, fontWeight = FontWeight.Bold)
        TimeUnitChip(minutes)
        Text(":", color = NovaWhite, fontWeight = FontWeight.Bold)
        TimeUnitChip(seconds)
    }
}

@Composable
private fun TimeUnitChip(value: String) {
    Surface(
        color = NovaNavy,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.border(1.dp, NovaCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = value,
            color = NovaWhite,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ShimmerLoadingPlaceholder(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEFF4FA),
            NovaCyanSoft,
            NovaPinkSoft.copy(alpha = 0.7f),
            Color(0xFFEFF4FA)
        ),
        start = Offset(translate - 300f, 0f),
        end = Offset(translate, 260f)
    )
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(brush)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = NovaNavy
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            Surface(
                onClick = onActionClick,
                shape = CircleShape,
                color = NovaPinkSoft
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = NovaPink,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
