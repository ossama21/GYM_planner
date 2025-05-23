package com.H_Oussama.gymplanner.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Title text component
 */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Subtitle text component
 */
@Composable
fun SubtitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Body text component
 */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Label text component
 */
@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Caption text component
 */
@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Badge component with customizable background and text colors
 */
@Composable
fun TextBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Creates a highlighted text where parts matching [highlightText] are highlighted
 */
@Composable
fun HighlightedText(
    text: String,
    highlightText: String,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    baseColor: Color = MaterialTheme.colorScheme.onSurface,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    if (highlightText.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            style = baseStyle,
            color = baseColor
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        val normalStyle = baseStyle.toSpanStyle().copy(color = baseColor)
        val highlightStyle = baseStyle.toSpanStyle().copy(
            color = highlightColor,
            fontWeight = FontWeight.Bold
        )

        val textLowerCase = text.lowercase()
        val highlightLowerCase = highlightText.lowercase()
        
        var startIndex = 0
        while (true) {
            val highlightStart = textLowerCase.indexOf(highlightLowerCase, startIndex)
            if (highlightStart < 0) {
                // No more occurrences found
                withStyle(normalStyle) {
                    append(text.substring(startIndex))
                }
                break
            }
            
            // Add text before the highlight
            if (highlightStart > startIndex) {
                withStyle(normalStyle) {
                    append(text.substring(startIndex, highlightStart))
                }
            }
            
            // Add the highlighted part
            val highlightEnd = highlightStart + highlightText.length
            withStyle(highlightStyle) {
                append(text.substring(highlightStart, highlightEnd))
            }
            
            startIndex = highlightEnd
        }
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = baseStyle
    )
}

/**
 * Section title text styled consistently for the app
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/**
 * Accent text that highlights important information
 */
@Composable
fun AccentText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/**
 * A label with value text, useful for displaying key-value pairs
 */
@Composable
fun LabelWithValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(
            color = labelColor,
            fontWeight = FontWeight.Normal
        )) {
            append("$label: ")
        }
        withStyle(style = SpanStyle(
            color = valueColor,
            fontWeight = FontWeight.Bold
        )) {
            append(value)
        }
    }
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

/**
 * A text chip that displays small pieces of information
 */
@Composable
fun TextChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Text styled for stat values like workout stats
 */
@Composable
fun StatValueText(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )) {
                    append(value)
                }
                append("\n")
                withStyle(style = SpanStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )) {
                    append(label)
                }
            },
            textAlign = TextAlign.Center
        )
    }
} 