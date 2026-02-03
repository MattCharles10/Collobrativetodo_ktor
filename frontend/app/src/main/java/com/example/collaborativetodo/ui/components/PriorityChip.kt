package com.example.collaborativetodo.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.ui.theme.HighPriorityColor
import com.example.collaborativetodo.ui.theme.LowPriorityColor
import com.example.collaborativetodo.ui.theme.MediumPriorityColor


@Composable
fun PriorityChip(
    priority: Int, // 0: low, 1: medium, 2: high
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val (text, color) = when (priority) {
        0 -> Pair("Low", LowPriorityColor)
        1 -> Pair("Medium", MediumPriorityColor)
        2 -> Pair("High", HighPriorityColor)
        else -> Pair("Unknown", Color.Gray)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }
    }
}