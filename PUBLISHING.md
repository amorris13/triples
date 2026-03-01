# Publishing to Google Play Store

This project is configured to use **Fastlane** and **GitHub Actions** for automated publishing to the Google Play Console (Beta track).

## Setup Instructions

To use this automation, you need to configure several secrets in your GitHub repository settings (**Settings > Secrets and variables > Actions**).

### 1. Google Play Service Account
You need a service account JSON key to authorize Fastlane to upload to the Play Store.

1.  Follow the [Fastlane Google Play setup guide](https://docs.fastlane.tools/getting-started/android/setup/#setting-up-google-play-api-access).
2.  Once you have the JSON key file, encode it to Base64:
    ```bash
    base64 -i your-service-account-key.json
    ```
3.  Add the Base64 string as a GitHub Secret named **`GPC_JSON_KEY_BASE64`**.

### 2. Android App Signing (Keystore)
You need to provide your release keystore and its credentials.

1.  Encode your `.jks` or `.keystore` file to Base64:
    ```bash
    base64 -i your-release-key.jks
    ```
2.  Add the following GitHub Secrets:
    -   **`RELEASE_KEYSTORE_BASE64`**: The Base64 string of your keystore file.
    -   **`RELEASE_STORE_PASSWORD`**: The password for your keystore.
    -   **`RELEASE_KEY_ALIAS`**: The alias for your key.
    -   **`RELEASE_KEY_PASSWORD`**: The password for your key.

## How to Trigger a Release

1.  Update the `versionCode` and `versionName` in `app/build.gradle` if necessary.
2.  Commit and push your changes.
3.  Go to the **Actions** tab in your GitHub repository.
4.  Select the **Publish to Play Store** workflow on the left.
5.  Click the **Run workflow** button.

The workflow will build the signed AAB and upload it, along with the metadata in the `fastlane/metadata/` directory, to the Beta track in the Google Play Console.
