# expressive-widget-lab

Production-ready Android Studio sample showing a modern home screen widget built with Jetpack Glance, Jetpack Compose, Material 3 expressive theming, WorkManager, and MVVM.

## Stack

- Kotlin
- Android Gradle Plugin 9.0.0
- Gradle 9.3.1
- Compose BOM 2026.02.01
- Glance 1.1.1
- WorkManager 2.10.5
- Target SDK 36
- Min SDK 26

## Open In Android Studio

1. Open Android Studio from `D:\AS\bin\studio64.exe`.
2. Choose **Open**.
3. Select `C:\Users\Om\OneDrive\Documents\New project\worktrees\expressive-widget-lab`.
4. When Studio asks for a JDK, pick a JDK 17 install.
5. Let Gradle sync and install any missing Android SDK 36 packages.

## Run

1. Start an emulator or connect a device running Android 8.0 or newer.
2. Click **Run 'app'** in Android Studio.
3. After install, long-press the launcher home screen.
4. Open the widgets picker.
5. Drag **Expressive Clock Widget** onto the home screen.
6. Tap the widget anytime to refresh it immediately.

## Upgrade Dependencies Later

1. Update plugin versions in `build.gradle.kts`.
2. Update the wrapper in `gradle/wrapper/gradle-wrapper.properties`.
3. Update library versions in `app/build.gradle.kts`.
4. Sync the project and run the app again.

## Architecture

- `MainActivity` hosts the Compose UI.
- `MainViewModel` schedules and refreshes widgets from the app layer.
- `ExpressiveClockWidget` renders widget UI and stays passive.
- `ExpressiveWidgetState` persists Glance widget state in preferences.
- `ExpressiveWidgetReceiver` handles widget lifecycle hooks.
- `WidgetUpdateWorker` owns periodic updates with WorkManager.
- `ui/theme` defines the app's expressive Material 3 theme.
