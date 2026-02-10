package com.example.finvovo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class TutorialStep(
    val targetRect: Rect?,
    val title: String,
    val description: String,
    val onNext: () -> Unit = {}
)

@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    if (currentStepIndex >= steps.size) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    val currentStep = steps[currentStepIndex]
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    // Animatable values for smoothness
    val targetRect = currentStep.targetRect ?: Rect(
        left = screenWidth / 2,
        top = screenHeight / 2,
        right = screenWidth / 2,
        bottom = screenHeight / 2
    )

    val animLeft = remember { Animatable(targetRect.left) }
    val animTop = remember { Animatable(targetRect.top) }
    val animRight = remember { Animatable(targetRect.right) }
    val animBottom = remember { Animatable(targetRect.bottom) }

    LaunchedEffect(currentStep) {
        launch { animLeft.animateTo(targetRect.left, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
        launch { animTop.animateTo(targetRect.top, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
        launch { animRight.animateTo(targetRect.right, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
        launch { animBottom.animateTo(targetRect.bottom, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
    }

    val currentRect = Rect(
        left = animLeft.value,
        top = animTop.value,
        right = animRight.value,
        bottom = animBottom.value
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume clicks */ }
    ) {
        // Scrim with Hole
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f)) {
            // Draw dimmed background
            drawRect(Color.Black.copy(alpha = 0.6f))
            
            // Cut out the hole
            if (currentStep.targetRect != null) {
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(currentRect.left - 8.dp.toPx(), currentRect.top - 8.dp.toPx()),
                    size = Size(currentRect.width + 16.dp.toPx(), currentRect.height + 16.dp.toPx()),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()), // Rounded corners for target
                    blendMode = BlendMode.Clear
                )
            }
        }

        // Tooltip
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Position tooltip smartly (above or below target)
            val isTargetInTopHalf = currentRect.center.y < screenHeight / 2
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .align(if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter)
                    .padding(bottom = if (isTargetInTopHalf) 48.dp else 0.dp, top = if (!isTargetInTopHalf) 48.dp else 0.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentStep.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Skip", color = Color.Gray)
                        }
                        
                        Button(
                            onClick = {
                                currentStep.onNext()
                                onStepChange(currentStepIndex + 1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.finvovo.ui.theme.PrimaryViolet)
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}
