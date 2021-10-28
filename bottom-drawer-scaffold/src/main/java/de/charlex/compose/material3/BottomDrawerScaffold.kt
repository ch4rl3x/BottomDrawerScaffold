package de.charlex.compose.material3

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.swipeable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import de.charlex.compose.BottomDrawerScaffoldStack
import de.charlex.compose.BottomDrawerScaffoldState
import de.charlex.compose.BottomDrawerValue
import de.charlex.compose.Scrim
import de.charlex.compose.calculateFraction
import de.charlex.compose.rememberBottomDrawerScaffoldState
import kotlinx.coroutines.launch

@Composable
@ExperimentalMaterial3Api
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
    drawerGesturesEnabled: Boolean? = null,
    drawerShape: Shape = androidx.compose.material.MaterialTheme.shapes.large,
    drawerScrimColor: Color = BottomDrawerScaffoldDefaults.scrimColor,
    drawerElevation: Dp = BottomDrawerScaffoldDefaults.DrawerElevation,
    drawerBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    drawerContentColor: Color = MaterialTheme.colorScheme.contentColorFor(drawerBackgroundColor),
    drawerPeekHeight: Dp = BottomDrawerScaffoldDefaults.DrawerPeekHeight,
    drawerContent: @Composable ColumnScope.() -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(backgroundColor),
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
        floatingActionButtonPosition = floatingActionButtonPosition
    ) { scaffoldPaddingValues ->
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
                                    content(PaddingValues(bottom = max(drawerPeekHeight, scaffoldPaddingValues.calculateBottomPadding())))

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
                            tonalElevation = drawerElevation,
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
        get() = MaterialTheme.colorScheme.onSurface.copy(alpha = ScrimOpacity)

    const val ScrimOpacity = 0.32f
}
