# Triples UI Testing

This project uses Espresso and AndroidX Test for UI testing, along with a custom screenshot comparison utility for snapshot testing.

## Prerequisites

- An Android device or emulator (emulator is recommended for consistent screenshots).

## Running Tests

To run the UI tests in their default "comparison" mode (comparing against reference screenshots in `app/src/androidTest/assets/goldens/`):

```bash
./gradlew connectedDebugAndroidTest
```

## Recording/Updating Golden Screenshots

If you have added new UI or changed existing UI, you'll need to update the "golden" (reference) screenshots:

1.  **Run tests in record mode:**
    ```bash
    ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.recordMode=true
    ```
    This will save new screenshots to the device at `/sdcard/Android/data/com.antsapps.triples/files/screenshots/`.

2.  **Pull screenshots from the device:**
    ```bash
    adb pull /sdcard/Android/data/com.antsapps.triples/files/screenshots/ .
    ```

3.  **Move the screenshots to the assets folder:**
    Move the `.png` files from your local `screenshots/` directory to:
    `app/src/androidTest/assets/goldens/`

4.  **Check-in the new golden images:**
    Commit the updated PNG files along with your code changes.

## Test Flows Covered

- `GameFlowTest`: Covers starting new Classic and Arcade games.
- `SettingsAndHelpTest`: Covers navigating to the Settings and Help screens.

## Screenshot Comparison Logic

The `ScreenshotComparator` utility performs a pixel-by-pixel comparison between the captured screenshot and the golden reference. It allows for a 1% pixel difference tolerance to account for minor rendering variations (e.g., anti-aliasing).

If a comparison fails, the "failed" screenshot is saved to the device's `screenshots/` directory with a `_failed` suffix for debugging.
