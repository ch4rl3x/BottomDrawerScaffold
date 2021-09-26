# BottomDrawerScaffold
## Current Compose Version: 1.0.2


Compose BottomDrawerScaffold which implements the Material Bottom Drawer

https://material.io/components/navigation-drawer#bottom-drawer



The Drawer stays visible in collapsed state to show header or quick access features.

Edge-To-Edge support



<a href="https://github.com/ch4rl3x/BottomDrawerScaffold/actions?query=workflow%3ALint"><img src="https://github.com/ch4rl3x/BottomDrawerScaffold/workflows/Lint/badge.svg" alt="Lint"></a>
<a href="https://github.com/ch4rl3x/BottomDrawerScaffold/actions?query=workflow%3AKtlint"><img src="https://github.com/ch4rl3x/BottomDrawerScaffold/workflows/Ktlint/badge.svg" alt="Ktlint"></a>

<a href="https://www.codefactor.io/repository/github/ch4rl3x/BottomDrawerScaffold"><img src="https://www.codefactor.io/repository/github/ch4rl3x/BottomDrawerScaffold/badge" alt="CodeFactor" /></a>
<a href="https://repo1.maven.org/maven2/de/charlex/compose/bottom-drawer-scaffold/"><img src="https://img.shields.io/maven-central/v/de.charlex.compose/bottom-drawer-scaffold" alt="Maven Central" /></a>


# Add to your project

Add actual BottomDrawerScaffold library:

```groovy
dependencies {
    implementation 'de.charlex.compose:bottom-drawer-scaffold:1.0.0-rc01'
}
```

# How does it work?

Use like any other Scaffold

```kotlin
BottomDrawerScaffold(
    modifier = Modifier
    topBar = {                  //Optional
        ...
    },
    bottomBar = {               //Optional
        ...
    },
    floatingActionButton = {    //Optional
        ...
    },
    isFloatingActionButtonDocked = true,
    floatingActionButtonPosition = FabPosition.End,
    																//Add drawerTopInset for Edge-To-Edge Suppport
    scaffoldState = rememberBottomDrawerScaffoldState(drawerTopInset = LocalWindowInsets.current.statusBars.top), 
    snackbarHost = {
        ...
    },
    drawerModifier = Modifier,
    drawerGesturesEnabled = true,
    drawerPeekHeight = 150.dp,
    drawerBackgroundColor = Color.Transparent,  //Transparent drawer for custom Drawer shape
    drawerElevation = 0.dp,
    drawerContent = {
        Surface(                    //To add Padding to Drawer
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    top = 10.dp
                ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            elevation = 4.dp
        ) {
            ...
        }
    }
) {
    content()
}
```

# Preview

![BottomDrawerScaffold](https://github.com/ch4rl3x/BottomDrawerScaffold/blob/main/art/bottom-drawer-scaffold.gif)


That's it!

License
--------

    Copyright 2021 Alexander Karkossa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
