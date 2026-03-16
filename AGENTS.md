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
