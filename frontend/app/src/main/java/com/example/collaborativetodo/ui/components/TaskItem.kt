package com.example.collaborativetodo.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.collaborativetodo.data.dtos.Task



@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onTaskClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Filled.CheckCircle
                    else Icons.Outlined.Circle,
                    contentDescription = if (task.isCompleted) "Mark as incomplete"
                    else "Mark as complete",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title with priority
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                    )

                    PriorityChip(priority = task.priority)
                }

                // Description
                task.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Category and due date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    task.category?.let { category ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    task.formattedDueDate?.let { dueDate ->
                        Text(
                            text = "Due: $dueDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Shared status
                if (task.sharedWith.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Shared with ${task.sharedWith.size} person${if (task.sharedWith.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}