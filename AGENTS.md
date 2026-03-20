# Agent Instructions

## Formatting
This project uses Spotless to enforce code formatting. Always run the following command before submitting your changes to ensure they follow the project's style guidelines:

```bash
./gradlew :app:spotlessApply
```

## Release Notes (Changelogs)
When making significant changes to the app, you must update the release notes.
1. Locate the changelog directory for the relevant locale (e.g., `fastlane/metadata/android/en-US/changelogs/`).
2. Create or update a text file named exactly after the current `versionCode` found in `app/build.gradle` (e.g., `13.txt`).
3. Alternatively, you can update `default.txt` in the same directory to provide a fallback changelog.

## Testing
To ensure the stability of the application, you must run tests before submitting any changes.

### Unit Tests
Run the standard unit tests using:
```bash
./gradlew test
```

In Claude Code on the web, the `env-setup.sh` script pre-downloads Robolectric
jars and configures offline mode via `~/.gradle/gradle.properties`, so no extra
flags are needed.

### Screenshot Tests (Roborazzi)
This project uses Roborazzi for screenshot testing.

- **Verify screenshots:** To check if your changes have caused any visual regressions, run:
  ```bash
  ./gradlew verifyRoborazziDebug
  ```
- **Update/Record screenshots:** If you have intentionally changed the UI and need to update the reference screenshots, run:
  ```bash
  ./gradlew recordRoborazziDebug
  ```
  Note: This will overwrite the existing screenshots in `app/src/test/screenshots/`.

### UI Quality
Always record screenshots using `./gradlew recordRoborazziDebug` when making UI changes and reflect on them to ensure the design is of high quality.
