package de.charlex.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.charlex.compose.BottomDrawerScaffold
import de.charlex.compose.rememberBottomDrawerScaffoldState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Content() {
    MaterialTheme {

        val topBarHeight = with(LocalDensity.current) { 56.dp.toPx() }

        BottomDrawerScaffold(
            scaffoldState = rememberBottomDrawerScaffoldState(drawerTopInset = topBarHeight.toInt()),
            topBar = {
                TopAppBar {
                    Text("Test")
                }
            },
            bottomBar = {
                BottomAppBar(
                    cutoutShape = CircleShape
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, "Menu icon")
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                    },
                ) {
                    Icon(Icons.Filled.Add, "Add icon")
                }
            },
            backgroundColor = Color(0xFFF2F2F2),
            drawerBackgroundColor = Color.Transparent,
            drawerElevation = 0.dp,
            drawerPeekHeight = 180.dp,
            drawerContent = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            top = 10.dp
                        ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(ScrollState(initial = 0)) // No remeber. We want the list always start at 0
                            .fillMaxSize(),
                    ) {

                        Group(title = "Category A") {
                            RowEntry(icon = Icons.Default.Build, label = "Build the app")
                            RowEntry(icon = Icons.Default.Send, label = "Send a message")
                            RowEntry(icon = Icons.Default.PlayArrow, label = "Play some stuff")
                            RowEntry(icon = Icons.Default.Phone, label = "Call a person")
                        }

                        Group(title = "Setting") {
                            RowEntry(icon = Icons.Default.Email, label = "Email config")
                            RowEntry(icon = Icons.Default.DateRange, label = "Date Range")
                            RowEntry(icon = Icons.Default.ShoppingCart, label = "Subscriptions")
                        }

                        Group(title = "Other") {
                            RowEntry(icon = Icons.Default.Place, label = "Place or Location")
                            RowEntry(icon = Icons.Default.LocationOn, label = "Some text here")
                        }

                        Group(title = "Impressum") {
                            RowEntry(icon = Icons.Default.Email, label = "Write Mail to the developer")
                            RowEntry(icon = Icons.Default.Refresh, label = "Refresh")
                            RowEntry(icon = Icons.Default.List, label = "List items")
                            RowEntry(icon = Icons.Default.Delete, label = "Delete item")
                            RowEntry(icon = Icons.Default.Info, label = "Info about the app")
                            RowEntry(icon = Icons.Default.Home, label = "Home")
                            RowEntry(icon = Icons.Default.Check, label = "Check")
                        }

                        Spacer(modifier = Modifier.requiredHeight(56.dp))   //FIXME find a better solution
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.h6
                )
            }

        }
    }
}

@Composable
fun Group(
    title: String,
    children: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(text = title,
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .padding(top = 30.dp, bottom = 15.dp),
        )
        Column(content = children)
    }
}

@Composable
fun RowEntry(
    icon: ImageVector?,
    label: String,
) {
    Row(
        modifier = Modifier.padding(
            start = 40.dp,
            end = 40.dp,
            top = 10.dp,
            bottom = 10.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon != null) {
            Icon(
                icon,
                tint = MaterialTheme.colors.primaryVariant,
                contentDescription = "Menu icon for $label"
            )
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }


        Text(
            text = label,
            modifier = Modifier.padding(start = 15.dp),
            color = MaterialTheme.colors.onSurface,
            maxLines = 2
        )
    }
}

@Preview
@Composable
fun ContentPreview() {
    Content()
}