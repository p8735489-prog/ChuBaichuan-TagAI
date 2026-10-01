package Shirakawa.LocalCueWord.ui.theme

// 从 local-dream 源码复制，未作任何逻辑改动。
// 来源：local-dream-master/app/src/main/java/io/github/xororz/localdream/ui/theme/Motion.kt
// 仅替换 package 路径。

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * MD3 motion tokens: durations and easings shared across the app.
 * Use [Motion] specs for ad-hoc animations and the helpers below for navigation transitions.
 */
object Motion {
    // Duration tokens (ms) — match MD3 spec
    const val DurationShort = 200 // standard / emphasized accelerate (exit)
    const val DurationMedium = 300 // standard (both directions)
    const val DurationLong = 400 // emphasized decelerate (enter)
    const val DurationExtraLong = 500 // emphasized (begin & end on screen)

    // Easing tokens
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val StandardDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    // Tween specs (fallback when spring physics is not appropriate)
    val Fade: FiniteAnimationSpec<Float> = tween(DurationLong, easing = EmphasizedDecelerate)
    val FadeOut: FiniteAnimationSpec<Float> = tween(DurationShort, easing = EmphasizedAccelerate)

    // Linear catch-up between discrete progress samples.
    val Progress: FiniteAnimationSpec<Float> = tween(DurationMedium, easing = LinearEasing)

    val Slide: FiniteAnimationSpec<IntOffset> = tween(DurationLong, easing = Emphasized)
    val Expand: FiniteAnimationSpec<IntSize> = tween(DurationLong, easing = EmphasizedDecelerate)
    val Shrink: FiniteAnimationSpec<IntSize> = tween(DurationShort, easing = EmphasizedAccelerate)

    // Linear specs for gesture-driven seeking (e.g. NavHost predictive back).
    // Non-linear easings make the seek feel non-1:1 against finger progress.
    val SlideLinear: FiniteAnimationSpec<IntOffset> = tween(DurationLong, easing = LinearEasing)
    val FadeLinear: FiniteAnimationSpec<Float> = tween(DurationLong, easing = LinearEasing)

    // Spring specs — expressive motion (low damping, high bounce)
    fun <T> springExpressiveSpatial(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> springExpressiveEffects(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

// ---------- NavHost shared-axis X transitions ----------
// Forward = next screen slides in from the trailing edge; back = previous slides in from the leading edge.

fun AnimatedContentTransitionScope<*>.sharedAxisXEnter(): EnterTransition = slideIntoContainer(SlideDirection.Start, animationSpec = Motion.Slide) +
    fadeIn(animationSpec = Motion.Fade)

fun AnimatedContentTransitionScope<*>.sharedAxisXExit(): ExitTransition = slideOutOfContainer(SlideDirection.Start, animationSpec = Motion.Slide) +
    fadeOut(animationSpec = Motion.FadeOut)

fun AnimatedContentTransitionScope<*>.sharedAxisXPopEnter(): EnterTransition = slideIntoContainer(SlideDirection.End, animationSpec = Motion.Slide) +
    fadeIn(animationSpec = Motion.Fade)

fun AnimatedContentTransitionScope<*>.sharedAxisXPopExit(): ExitTransition = slideOutOfContainer(SlideDirection.End, animationSpec = Motion.Slide) +
    fadeOut(animationSpec = Motion.FadeOut)

// Predictive back variants: NavHost seeks these by gesture progress, so the
// underlying spec must be linear — otherwise an emphasized curve maps a small
// finger drag to a large slide (e.g. Emphasized(0.2) ≈ 0.5).
fun AnimatedContentTransitionScope<*>.sharedAxisXPredictivePopEnter(): EnterTransition = slideIntoContainer(SlideDirection.End, animationSpec = Motion.SlideLinear) +
    fadeIn(animationSpec = Motion.FadeLinear)

fun AnimatedContentTransitionScope<*>.sharedAxisXPredictivePopExit(): ExitTransition = slideOutOfContainer(SlideDirection.End, animationSpec = Motion.SlideLinear) +
    fadeOut(animationSpec = Motion.FadeLinear)

// ---------- AnimatedVisibility shared-axis X (for modal overlays) ----------
// Slides in from trailing edge and out to trailing edge — mirrors NavHost's
// forward navigation feel for screen-like overlays (e.g. settings panel).

val sharedAxisXEnterVisibility: EnterTransition =
    slideInHorizontally(animationSpec = Motion.Slide, initialOffsetX = { it }) +
        fadeIn(animationSpec = Motion.Fade)

val sharedAxisXExitVisibility: ExitTransition =
    slideOutHorizontally(animationSpec = Motion.Slide, targetOffsetX = { it }) +
        fadeOut(animationSpec = Motion.FadeOut)