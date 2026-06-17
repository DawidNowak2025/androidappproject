# GROW Garden Tracker Android

This is a standalone native Android app built with Kotlin, Jetpack Compose, Material 3, Room Database, DataStore, Coil, coroutines, Navigation Compose, ViewModels, and repositories.

## Standalone App

This Android project does not require the old React project, Node.js, Express, or any backend server. All garden data is stored locally on the Android device.

## Local Storage

The app uses Room Database for users, plants, garden zones, watering history, and activity logs. It uses DataStore to remember the logged-in user session.

## Open In Android Studio

1. Open Android Studio.
2. Choose Open.
3. Select this folder: `GROW_Garden_Tracker_Android`.
4. Let Gradle sync.
5. Press Run.

## Run On Emulator

Create or choose an Android emulator and press Run. No network setup is required because the database is local.

## Run On Physical Phone

Enable USB debugging, connect the phone, choose the device in Android Studio, and press Run. The app works offline and does not need the phone to be on the same Wi-Fi as the computer.

## Add Plants

Register a local account, log in, open Plants, and choose Add Plant. Select a garden zone, enter watering dates, and optionally pick plant and location images. Picked images are copied into the app internal storage.

## Export And Import JSON

Open Settings and use Export JSON to save a local backup text file in the app storage. Use Import JSON to restore from that backup. Import/export records activity log entries.

## Common Build Issues

If Gradle sync fails, make sure Android Studio is up to date and has Android SDK 35 installed. If Room generated code is missing, run Gradle sync again so kapt can generate DAO implementations. If image picking returns no image, try another gallery app or photo source.
