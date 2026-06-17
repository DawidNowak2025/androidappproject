# GROW Garden Tracker Android

GROW Garden Tracker is a standalone native Android app built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, Navigation Compose, ViewModels, Coil and local repositories.

## Standalone App

This project does not need the legacy web project, a computer IP address, or a custom server. Users, plants, zones, photos, map pins, watering history, activity logs and JSON backups are stored locally on the Android device.

## Local Storage

Room stores users, plants, garden zones, watering history and activity logs. DataStore stores the logged-in user session. Picked images are copied into app internal storage and the saved local file path is stored in Room.

## Plant Knowledge

Plant Knowledge uses the public Wikipedia REST search endpoint:

`https://en.wikipedia.org/w/rest.php/v1/search/page`

This is the only feature that needs internet access. Search for a plant name, view title, description, excerpt and thumbnail, then open the page in the browser with the Open on Wikipedia button.

## AI Plant Care Assistant

The AI Plant Care Assistant is local and rule-based. It does not call an online AI model. It reads local app data such as plant name, type, notes, zone, watering dates, watering history and activity logs, then generates practical care advice.

## Weather Feature Removed

The previous weather feature has been removed. There is no weather API and no weather screen.

## Open In Android Studio

1. Open Android Studio.
2. Choose Open.
3. Select `C:\Users\fdf\Desktop\ANDROIDGARDEN\GROW_Garden_Tracker_Android`.
4. Let Gradle sync.
5. Press Run.

## Test Plant Knowledge

1. Log in.
2. Open Plant Knowledge.
3. Search for `tomato` or another plant.
4. Confirm Wikipedia results load.
5. Tap Open on Wikipedia.

## Test AI Assistant

1. Add at least one plant.
2. Add notes or watering history if available.
3. Open AI Assistant.
4. Select a plant.
5. Confirm local care advice appears.

## Backups

Open Settings and use Export JSON to save a local backup text file in app storage. Use Import JSON to restore from that backup.

## Common Build Issues

Use Android Studio with Android SDK 35 installed. If Room generated code is missing, run Gradle sync again so kapt can generate DAO implementations.
