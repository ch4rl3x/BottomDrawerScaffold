package de.charlex.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.contentColorFor
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@ExperimentalMaterialApi
fun BottomDrawerScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    gesturesEnabled: Boolean = true,
    drawerModifier: Modifier = Modifier,
    scaffoldState: BottomDrawerScaffoldState = rememberBottomDrawerScaffoldState(),
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    floatingActionButton: @Composable (() -> Unit)? = null,
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = true,
    drawerGesturesEnabled: Boolean? = null,
    drawerShape: Shape = MaterialTheme.shapes.large,
    drawerScrimColor: Color = BottomDrawerScaffoldDefaults.scrimColor,
    drawerElevation: Dp = BottomDrawerScaffoldDefaults.DrawerElevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    drawerPeekHeight: Dp = BottomDrawerScaffoldDefaults.DrawerPeekHeight,
    drawerContent: @Composable ColumnScope.() -> Unit,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable (PaddingValues) -> Unit
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            bottomBar?.invoke()
        },
        floatingActionButton = {
            floatingActionButton?.invoke()
        },
        isFloatingActionButtonDocked = floatingActionButton != null && isFloatingActionButtonDocked,
        floatingActionButtonPosition = floatingActionButtonPosition
    ) {
        BoxWithConstraints(
            modifier = Modifier,
            contentAlignment = Alignment.BottomCenter
        ) {
            val fullHeight = constraints.maxHeight.toFloat()
            val peekHeightPx = with(LocalDensity.current) { drawerPeekHeight.toPx() }
            var bottomDrawerHeight by remember { mutableStateOf(fullHeight) }

            val swipeable = drawerModifier
                .nestedScroll(scaffoldState.bottomDrawerState.nestedScrollConnection)
                .swipeable(
                    state = scaffoldState.bottomDrawerState,
                    anchors = mapOf(
                        fullHeight - peekHeightPx to BottomDrawerValue.Collapsed,
                        fullHeight - bottomDrawerHeight + scaffoldState.bottomDrawerState.drawerTopInset to BottomDrawerValue.Expanded
                    ),
                    orientation = Orientation.Vertical,
                    enabled = drawerGesturesEnabled ?: gesturesEnabled,
                    resistance = null
                )
                .semantics {
                    if (peekHeightPx != bottomDrawerHeight) {
                        if (scaffoldState.bottomDrawerState.isCollapsed) {
                            expand {
                                if (scaffoldState.bottomDrawerState.confirmStateChange(
                                        BottomDrawerValue.Expanded
                                    )
                                ) {
                                    scope.launch { scaffoldState.bottomDrawerState.expand() }
                                }
                                true
                            }
                        } else {
                            collapse {
                                if (scaffoldState.bottomDrawerState.confirmStateChange(
                                        BottomDrawerValue.Collapsed
                                    )
                                ) {
                                    scope.launch { scaffoldState.bottomDrawerState.collapse() }
                                }
                                true
                            }
                        }
                    }
                }

            val child = @Composable {
                BottomDrawerScaffoldStack(
                    body = {
                        Surface(
                            color = backgroundColor,
                            contentColor = contentColor
                        ) {
                            Column(Modifier.fillMaxSize()) {
                                topBar?.invoke()

                                Box {
                                    content(PaddingValues(bottom = drawerPeekHeight))

                                    Scrim(
                                        open = scaffoldState.bottomDrawerState.isExpanded,
                                        onClose = {
                                            if (
                                                gesturesEnabled &&
                                                scaffoldState.bottomDrawerState.confirmStateChange(
                                                    BottomDrawerValue.Collapsed
                                                )
                                            ) {
                                                scope.launch { scaffoldState.bottomDrawerState.collapse() }
                                            }
                                        },
                                        fraction = {
                                            calculateFraction(fullHeight - peekHeightPx, fullHeight - bottomDrawerHeight + scaffoldState.bottomDrawerState.drawerTopInset, scaffoldState.bottomDrawerState.offset.value)
                                        },
                                        color = drawerScrimColor
                                    )
                                }
                            }
                        }
                    },
                    bottomDrawer = {
                        Surface(
                            swipeable
                                .fillMaxWidth()
                                .requiredHeightIn(min = drawerPeekHeight)
                                .onGloballyPositioned {
                                    bottomDrawerHeight = it.size.height.toFloat()
                                },
                            shape = drawerShape,
                            elevation = drawerElevation,
                            color = drawerBackgroundColor,
                            contentColor = drawerContentColor,
                            content = {
                                Column(content = drawerContent)
                            }
                        )
                    },
                    snackbarHost = {
                        Box {
                            snackbarHost(scaffoldState.snackbarHostState)
                        }
                    },
                    bottomDrawerOffset = scaffoldState.bottomDrawerState.offset,
                )
            }

            child()
        }
    }
}

@Composable
private fun Scrim(
    open: Boolean,
    onClose: () -> Unit,
    fraction: () -> Float,
    color: Color
) {
    val dismissDrawer = if (open) {
        Modifier
            .pointerInput(onClose) { detectTapGestures { onClose() } }
            .semantics(mergeDescendants = true) {
                contentDescription = "close drawer"
                onClick { onClose(); true }
            }
    } else {
        Modifier
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .then(dismissDrawer)
    ) {
        drawRect(color, alpha = fraction())
    }
}

private fun calculateFraction(a: Float, b: Float, pos: Float): Float {
    if (a == b) return 0f
    return ((pos - a) / (b - a)).coerceIn(0f, 1f)
}

@Composable
@ExperimentalMaterialApi
fun rememberBottomDrawerScaffoldState(
    drawerTopInset: Int = 0,
    bottomDrawerState: BottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Collapsed, drawerTopInset = drawerTopInset),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): BottomDrawerScaffoldState {
    return remember(bottomDrawerState, snackbarHostState) {
        BottomDrawerScaffoldState(
            bottomDrawerState = bottomDrawerState,
            snackbarHostState = snackbarHostState
        )
    }
}

@ExperimentalMaterialApi
@Stable
class BottomDrawerScaffoldState(
    val bottomDrawerState: BottomDrawerState,
    val snackbarHostState: SnackbarHostState
)

@Composable
@ExperimentalMaterialApi
fun rememberBottomDrawerState(
    initialValue: BottomDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (BottomDrawerValue) -> Boolean = { true },
    drawerTopInset: Int
): BottomDrawerState {
    return rememberSaveable(
        drawerTopInset,
        animationSpec,
        saver = BottomDrawerState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange,
            drawerTopInset = drawerTopInset
        )
    ) {
        BottomDrawerState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange,
            drawerTopInset = drawerTopInset
        )
    }
}

enum class BottomDrawerValue {
    /**
     * The bottom drawer is visible, but only showing its peek height.
     */
    Collapsed,

    /**
     * The bottom drawer is visible at its maximum height.
     */
    Expanded
}

@ExperimentalMaterialApi
@Stable
class BottomDrawerState(
    initialValue: BottomDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    val confirmStateChange: (BottomDrawerValue) -> Boolean = { true },
    val drawerTopInset: Int
) : SwipeableState<BottomDrawerValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {
    /**
     * Whether the bottom drawer is expanded.
     */
    val isExpanded: Boolean
        get() = currentValue == BottomDrawerValue.Expanded

    /**
     * Whether the bottom drawer is collapsed.
     */
    val isCollapsed: Boolean
        get() = currentValue == BottomDrawerValue.Collapsed

    /**
     * Expand the bottom drawer with animation and suspend until it if fully expanded or animation
     * has been cancelled. This method will throw [CancellationException] if the animation is
     * interrupted
     *
     * @return the reason the expand animation ended
     */
    suspend fun expand() = animateTo(BottomDrawerValue.Expanded)

    /**
     * Collapse the bottom drawer with animation and suspend until it if fully collapsed or animation
     * has been cancelled. This method will throw [CancellationException] if the animation is
     * interrupted
     *
     * @return the reason the collapse animation ended
     */
    suspend fun collapse() = animateTo(BottomDrawerValue.Collapsed)

    companion object {
        /**
         * The default [Saver] implementation for [BottomDrawerState].
         */
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (BottomDrawerValue) -> Boolean,
            drawerTopInset: Int
        ): Saver<BottomDrawerState, *> = Saver(
            save = { it.currentValue },
            restore = {
                BottomDrawerState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmStateChange = confirmStateChange,
                    drawerTopInset = drawerTopInset
                )
            }
        )
    }

    internal val nestedScrollConnection = this.createPreUpPostDownNestedScrollConnection(drawerTopInset)
}

@Composable
private fun BottomDrawerScaffoldStack(
    body: @Composable () -> Unit,
    bottomDrawer: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
    bottomDrawerOffset: State<Float>
) {
    Layout(
        content = {
            body()
            bottomDrawer()
            snackbarHost()
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)

            val (drawerPlaceable, snackbarPlaceable) =
                measurables.drop(1).map {
                    it.measure(constraints.copy(minWidth = 0, minHeight = 0))
                }

            val drawerOffsetY = bottomDrawerOffset.value.roundToInt()

            drawerPlaceable.placeRelative(0, drawerOffsetY)

            val snackbarOffsetX = (placeable.width - snackbarPlaceable.width) / 2
            val snackbarOffsetY = placeable.height - snackbarPlaceable.height

            snackbarPlaceable.placeRelative(snackbarOffsetX, snackbarOffsetY)
        }
    }
}

object BottomDrawerScaffoldDefaults {

    /**
     * The default elevation used by [BottomDrawerScaffold].
     */
    val DrawerElevation = 8.dp

    /**
     * The default peek height used by [BottomDrawerScaffold].
     */
    val DrawerPeekHeight = 56.dp

    val scrimColor: Color
        @Composable
        get() = MaterialTheme.colors.onSurface.copy(alpha = ScrimOpacity)

    const val ScrimOpacity = 0.32f
}
