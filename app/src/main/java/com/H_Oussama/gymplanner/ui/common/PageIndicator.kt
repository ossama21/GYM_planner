package com.H_Oussama.gymplanner.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A page indicator component displayed as a row of dots
 *
 * @param pageCount The total number of pages
 * @param currentPage The current page (0-based index)
 * @param onPageSelected Callback when a page is selected by clicking on its dot
 * @param modifier Modifier for the container
 * @param activeColor Color for the current page indicator
 * @param inactiveColor Color for inactive page indicators
 */
@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until pageCount) {
            val isSelected = i == currentPage
            
            Box(
                modifier = Modifier
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
                    .then(
                        if (onPageSelected != null) {
                            Modifier.clickable { onPageSelected(i) }
                        } else Modifier
                    )
            )
        }
    }
} 