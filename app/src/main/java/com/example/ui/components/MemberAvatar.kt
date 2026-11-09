package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.FamilyMember

fun parseColor(hex: String, fallback: Color = Color(0xFF0D9488)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

fun getMemberIcon(iconName: String, relation: String): ImageVector {
    return when (iconName) {
        "sports_esports" -> Icons.Default.SportsEsports
        "toys" -> Icons.Default.Toys
        "face_woman" -> Icons.Default.Face
        "face_man" -> Icons.Default.Person
        else -> when (relation.lowercase()) {
            "son" -> Icons.Default.SportsEsports
            "daughter" -> Icons.Default.Toys
            "mom" -> Icons.Default.Face
            else -> Icons.Default.Person
        }
    }
}

@Composable
fun MemberAvatar(
    name: String,
    colorHex: String,
    iconName: String = "",
    relation: String = "",
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val bg = parseColor(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        val icon = getMemberIcon(iconName, relation)
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

@Composable
fun FamilyFilterBar(
    members: List<FamilyMember>,
    selectedMemberId: Long,
    onSelectMember: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("family_filter_bar"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All Family" pill
            val isAllSelected = selectedMemberId == 0L
            FamilyPill(
                title = "All Family",
                icon = Icons.Default.Group,
                color = Color(0xFF10B981),
                isSelected = isAllSelected,
                onClick = { onSelectMember(0L) }
            )

            members.forEach { member ->
                Spacer(modifier = Modifier.width(8.dp))
                val isSelected = selectedMemberId == member.id
                FamilyPill(
                    title = member.name,
                    icon = getMemberIcon(member.avatarIcon, member.relation),
                    color = parseColor(member.avatarColorHex),
                    isSelected = isSelected,
                    onClick = { onSelectMember(member.id) }
                )
            }
        }
    }
}

@Composable
private fun FamilyPill(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderModifier = if (isSelected) {
        Modifier.border(1.5.dp, color, RoundedCornerShape(20.dp))
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(borderModifier)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
