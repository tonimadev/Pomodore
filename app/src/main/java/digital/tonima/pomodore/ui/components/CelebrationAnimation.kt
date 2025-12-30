package digital.tonima.pomodore.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.pomodore.ui.theme.CelebrationBlue
import digital.tonima.pomodore.ui.theme.CelebrationGold
import digital.tonima.pomodore.ui.theme.CelebrationOrange
import digital.tonima.pomodore.ui.theme.CelebrationPink
import digital.tonima.pomodore.ui.theme.CelebrationPurple
import kotlin.math.sin
import kotlin.random.Random

data class Confetti(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val angle: Float
)

@Composable
fun CelebrationAnimation(
    message: String,
    modifier: Modifier = Modifier
) {
    val confettiList = remember {
        List(50) {
            Confetti(
                x = Random.nextFloat(),
                y = -0.1f,
                color = listOf(
                    CelebrationGold,
                    CelebrationOrange,
                    CelebrationPink,
                    CelebrationPurple,
                    CelebrationBlue
                ).random(),
                size = Random.nextFloat() * 8f + 4f,
                speed = Random.nextFloat() * 0.02f + 0.01f,
                angle = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animationProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_fall"
    )

    val scaleAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiList.forEach { confetti ->
                val progress = animationProgress.value
                val currentY = confetti.y + progress * 1.2f

                if (currentY < 1.1f) {
                    drawConfetti(
                        confetti = confetti,
                        progress = progress,
                        currentY = currentY
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎉\n$message\n🎊",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (32.sp.value * scaleAnimation.value).sp
                ),
                color = CelebrationGold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 40.sp
            )
        }
    }
}

private fun DrawScope.drawConfetti(
    confetti: Confetti,
    progress: Float,
    currentY: Float
) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val x = (confetti.x + sin(progress * 10 + confetti.angle) * 0.05f) * canvasWidth
    val y = currentY * canvasHeight

    drawCircle(
        color = confetti.color.copy(alpha = 1f - progress * 0.3f),
        radius = confetti.size,
        center = Offset(x, y)
    )
}
