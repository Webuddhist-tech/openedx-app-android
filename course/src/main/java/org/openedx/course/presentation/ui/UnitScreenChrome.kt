package org.openedx.course.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.BlockType
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import java.text.NumberFormat

@Composable
fun UnitScreenHeader(
    subsectionName: String,
    unitName: String,
    unitCount: Int,
    listOpen: Boolean,
    pillEnabled: Boolean,
    onBackClick: () -> Unit,
    onPillClick: () -> Unit,
) {
    val showPill = pillEnabled && unitCount > 1
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .displayCutoutForLandscape()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (subsectionName.isNotEmpty()) {
                Text(
                    text = subsectionName,
                    color = if (MaterialTheme.appColors.isLight) {
                        MaterialTheme.appColors.textPrimaryVariant
                    } else {
                        MaterialTheme.appColors.textFieldHint
                    },
                    style = MaterialTheme.appTypography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (unitName.isNotEmpty()) {
                if (showPill) {
                    UnitNamePill(
                        modifier = Modifier.padding(top = 4.dp),
                        unitName = unitName,
                        listOpen = listOpen,
                        onClick = onPillClick
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics { heading() },
                        text = unitName,
                        color = unitHeadingColor(),
                        style = unitTitleStyle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        BackBtn(
            modifier = Modifier.align(Alignment.CenterStart),
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun UnitNamePill(
    modifier: Modifier,
    unitName: String,
    listOpen: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val accent = MaterialTheme.appColors.primary
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                selected = listOpen
            }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = unitName,
            color = unitHeadingColor(),
            style = unitTitleStyle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (pressed) 0.2f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (listOpen) 180f else 0f),
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = accent
            )
        }
    }
}

@Composable
fun UnitProgressDashes(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    if (count < 2) return
    val positionLabel = blockPositionLabel(selectedIndex, count, spoken = true)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 12.dp)
            .clearAndSetSemantics { contentDescription = positionLabel },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selectedIndex) {
                            MaterialTheme.appColors.primary
                        } else {
                            MaterialTheme.appColors.cardViewBorder
                        }
                    )
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UnitBlockHeader(
    title: String,
    blockType: BlockType,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val showPosition = count > 1
    val positionLabel = blockPositionLabel(index, count)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.appColors.primary.copy(alpha = 0.12f))
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(unitHeaderIcon(blockType)),
                contentDescription = null,
                tint = MaterialTheme.appColors.primary
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                modifier = Modifier.semantics { heading() },
                text = title,
                color = MaterialTheme.appColors.textDark,
                style = unitTitleStyle(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (showPosition) {
                Text(
                    modifier = Modifier.semantics { invisibleToUser() },
                    text = positionLabel,
                    color = MaterialTheme.appColors.textFieldHint,
                    style = MaterialTheme.appTypography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun blockPositionLabel(index: Int, count: Int, spoken: Boolean = false): String {
    val formatter = NumberFormat.getInstance()
    return stringResource(
        if (spoken) R.string.course_block_position_spoken else R.string.course_block_position,
        formatter.format(index + 1),
        formatter.format(count)
    )
}

fun unitHeaderIcon(blockType: BlockType): Int {
    return when (blockType) {
        BlockType.MULTILINGUAL_TEXT -> R.drawable.course_ic_unit_globe
        BlockType.PROBLEM -> R.drawable.course_ic_unit_pencil
        BlockType.DRAG_AND_DROP_V2 -> R.drawable.course_ic_unit_hand
        BlockType.SURVEY -> R.drawable.course_ic_unit_checklist
        BlockType.OPENASSESSMENT -> R.drawable.course_ic_unit_search
        BlockType.LIBRARY_CONTENT -> R.drawable.course_ic_unit_books
        BlockType.VIDEO -> R.drawable.course_ic_unit_play
        else -> R.drawable.course_ic_unit_document
    }
}

@Composable
private fun unitTitleStyle() = MaterialTheme.appTypography.titleMedium.copy(
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp
)

@Composable
private fun unitHeadingColor() = if (MaterialTheme.appColors.isLight) {
    MaterialTheme.appColors.textPrimaryVariant
} else {
    MaterialTheme.appColors.textDark
}

fun cardStroke(color: Color): Color = color.copy(alpha = 0.6f)
