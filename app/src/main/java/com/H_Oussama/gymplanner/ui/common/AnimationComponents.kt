package com.H_Oussama.gymplanner.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * An animated visibility component that fades and slides content in/out
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FadeSlideInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { -40 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(tween(300)) + slideOutVertically(
            targetOffsetY = { -40 },
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * An animated visibility component that scales content in/out with a bouncy effect
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScaleBounceInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis = 150)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * An expandable card that animates expanding/collapsing with a material design style
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableCard(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Column {
            // Header is always visible
            headerContent()
            
            // Expanded content with animation
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(150)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(150)
                ) + fadeOut(
                    animationSpec = tween(150)
                )
            ) {
                Column(content = expandedContent)
            }
        }
    }
}

/**
 * A composable that adds a pulsating effect to its content
 */
@Composable
fun PulseEffect(
    pulsing: Boolean,
    modifier: Modifier = Modifier,
    pulseColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pulsing) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "Pulse Scale"
    )
    
    Box(modifier = modifier.scale(scale)) {
        content()
    }
} 