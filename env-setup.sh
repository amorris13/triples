#!/bin/bash
set -e

# =============================================================================
# Claude Code Web — Android Development Environment Setup Script
# Targets: Android API 36, build-tools 36.0.0, Robolectric offline mode
# =============================================================================

# -----------------------------------------------------------------------------
# 1. Environment variables
# -----------------------------------------------------------------------------
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Persist for all future shell sessions
echo "export ANDROID_HOME=\"$HOME/Android/sdk\"" >> "$HOME/.bashrc"
echo 'export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"' >> "$HOME/.bashrc"

# -----------------------------------------------------------------------------
# 2. Fix JAVA_TOOL_OPTIONS proxy settings
#    The default nonProxyHosts includes *.google.com which causes sdkmanager
#    and Gradle to bypass the egress proxy for Google Maven / dl.google.com,
#    resulting in connection failures. Narrow it to just localhost.
# -----------------------------------------------------------------------------
if echo "${JAVA_TOOL_OPTIONS:-}" | grep -q 'nonProxyHosts'; then
    FIXED_OPTS=$(echo "$JAVA_TOOL_OPTIONS" | sed 's/-Dhttp\.nonProxyHosts=[^ ]*/-Dhttp.nonProxyHosts=localhost|127.0.0.1/')
    export JAVA_TOOL_OPTIONS="$FIXED_OPTS"
    echo "export JAVA_TOOL_OPTIONS=\"$FIXED_OPTS\"" >> "$HOME/.bashrc"
    echo "Fixed JAVA_TOOL_OPTIONS nonProxyHosts."
fi

# -----------------------------------------------------------------------------
# 3. Install Android command-line tools
# -----------------------------------------------------------------------------
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "Installing Android command-line tools..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    curl -fSL -o /tmp/sdk_tools.zip \
      https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip
    unzip -q /tmp/sdk_tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm /tmp/sdk_tools.zip
fi

# -----------------------------------------------------------------------------
# 4. Install platform-tools via curl (stable direct-download URL)
# -----------------------------------------------------------------------------
if [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    echo "Installing platform-tools..."
    curl -fSL -o /tmp/platform-tools.zip \
      https://dl.google.com/android/repository/platform-tools-latest-linux.zip
    unzip -q /tmp/platform-tools.zip -d "$ANDROID_HOME"
    rm /tmp/platform-tools.zip
fi

# -----------------------------------------------------------------------------
# 5. Install SDK packages via sdkmanager
#    Primary path: sdkmanager with fixed proxy settings.
#    Fallback: direct curl downloads if sdkmanager networking still fails.
# -----------------------------------------------------------------------------
echo "Accepting licenses..."
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true

echo "Installing platforms;android-36 and build-tools;36.0.0..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
    "platforms;android-36" \
    "build-tools;36.0.0" || {

    echo "sdkmanager failed — falling back to manual downloads..."

    # Fallback: build-tools 36.0.0
    if [ ! -d "$ANDROID_HOME/build-tools/36.0.0" ]; then
        echo "Downloading build-tools 36.0.0..."
        mkdir -p "$ANDROID_HOME/build-tools"
        curl -fSL -o /tmp/build-tools.zip \
          "https://dl.google.com/android/repository/build-tools_r36_linux.zip"
        unzip -q /tmp/build-tools.zip -d /tmp/build-tools-extract
        extracted=$(ls /tmp/build-tools-extract | head -1)
        mv "/tmp/build-tools-extract/$extracted" "$ANDROID_HOME/build-tools/36.0.0"
        rm -rf /tmp/build-tools.zip /tmp/build-tools-extract
    fi

    # Fallback: platform android-36
    if [ ! -d "$ANDROID_HOME/platforms/android-36" ]; then
        echo "Downloading platform android-36..."
        mkdir -p "$ANDROID_HOME/platforms"
        curl -fSL -o /tmp/platform.zip \
          "https://dl.google.com/android/repository/platform-36_r01.zip"
        unzip -q /tmp/platform.zip -d /tmp/platform-extract
        extracted=$(ls /tmp/platform-extract | head -1)
        mv "/tmp/platform-extract/$extracted" "$ANDROID_HOME/platforms/android-36"
        rm -rf /tmp/platform.zip /tmp/platform-extract
    fi
}

# -----------------------------------------------------------------------------
# 6. Configure Gradle global proxy settings
#    Writes to ~/.gradle/gradle.properties so every Gradle project picks
#    up proxy config without needing it in the repo's gradle.properties.
# -----------------------------------------------------------------------------
mkdir -p "$HOME/.gradle"

PROXY_HOST=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyHost=)\S+' | head -1)
PROXY_PORT=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyPort=)\S+' | head -1)

if [ -n "$PROXY_HOST" ] && [ -n "$PROXY_PORT" ]; then
    echo "Configuring Gradle global proxy ($PROXY_HOST:$PROXY_PORT)..."
    cat > "$HOME/.gradle/gradle.properties" <<EOF
# Auto-generated by setup script for Claude Code Web proxy environment
systemProp.http.proxyHost=$PROXY_HOST
systemProp.http.proxyPort=$PROXY_PORT
systemProp.http.nonProxyHosts=localhost|127.0.0.1
systemProp.https.proxyHost=$PROXY_HOST
systemProp.https.proxyPort=$PROXY_PORT
systemProp.https.nonProxyHosts=localhost|127.0.0.1
EOF
else
    # Create the file even without proxy so we can append Robolectric config
    touch "$HOME/.gradle/gradle.properties"
fi

# -----------------------------------------------------------------------------
# 7. Pre-download Robolectric instrumented jar for offline mode
#    Robolectric's MavenArtifactFetcher uses its OWN network stack (not
#    Gradle's proxy settings), so it fails in proxied environments.
#    Solution: download the jar with curl and configure offline mode.
# -----------------------------------------------------------------------------
ROBO_VERSION="16-robolectric-13921718-i7"
ROBO_JAR="android-all-instrumented-${ROBO_VERSION}.jar"
ROBO_CACHE_DIR="$HOME/.robolectric-jars"

if [ ! -f "$ROBO_CACHE_DIR/$ROBO_JAR" ]; then
    echo "Pre-downloading Robolectric jar ($ROBO_VERSION)..."
    mkdir -p "$ROBO_CACHE_DIR"
    curl -fSL -o "$ROBO_CACHE_DIR/$ROBO_JAR" \
      "https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/${ROBO_VERSION}/${ROBO_JAR}"
    echo "Robolectric jar cached at $ROBO_CACHE_DIR/$ROBO_JAR"
fi

# Append Robolectric offline config to global Gradle properties
if ! grep -q "robolectric.offline" "$HOME/.gradle/gradle.properties" 2>/dev/null; then
    cat >> "$HOME/.gradle/gradle.properties" <<EOF

# Robolectric offline mode — use pre-downloaded jars instead of network fetch
systemProp.robolectric.offline=true
systemProp.robolectric.dependency.dir=$ROBO_CACHE_DIR
EOF
    echo "Configured Robolectric offline mode."
fi

# -----------------------------------------------------------------------------
# 8. Signing keystore
#    Generate a dummy keystore so debug/release builds succeed.
#    The app's build.gradle reads signing config from env vars.
# -----------------------------------------------------------------------------
KEYSTORE="$CLAUDE_PROJECT_DIR/app/keystore.jks"
if [ ! -f "$KEYSTORE" ]; then
    echo "Generating signing keystore..."
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -alias key \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass password -keypass password \
        -dname "CN=Test, OU=Test, O=Test, L=Test, S=Test, C=US" \
        > /dev/null 2>&1
fi

# Export signing env vars so Gradle can find them
export RELEASE_STORE_PASSWORD=password
export RELEASE_KEY_ALIAS=key
export RELEASE_KEY_PASSWORD=password

echo 'export RELEASE_STORE_PASSWORD=password' >> "$HOME/.bashrc"
echo 'export RELEASE_KEY_ALIAS=key' >> "$HOME/.bashrc"
echo 'export RELEASE_KEY_PASSWORD=password' >> "$HOME/.bashrc"

# Also write to CLAUDE_ENV_FILE if available (picked up by Claude Code hooks)
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
    echo 'export RELEASE_STORE_PASSWORD=password' >> "$CLAUDE_ENV_FILE"
    echo 'export RELEASE_KEY_ALIAS=key' >> "$CLAUDE_ENV_FILE"
    echo 'export RELEASE_KEY_PASSWORD=password' >> "$CLAUDE_ENV_FILE"
    echo "export ANDROID_HOME=$ANDROID_HOME" >> "$CLAUDE_ENV_FILE"
fi

# -----------------------------------------------------------------------------
# 9. Write local.properties for Gradle SDK resolution
# -----------------------------------------------------------------------------
echo "sdk.dir=$ANDROID_HOME" > local.properties

# -----------------------------------------------------------------------------
# 10. Warm Gradle caches
#     Resolve all dependencies and compile ahead of time so subsequent
#     builds are fast.
# -----------------------------------------------------------------------------
echo "Warming Gradle caches (assembleDebug)..."
cd "$CLAUDE_PROJECT_DIR"
./gradlew :app:assembleDebug --quiet 2>/dev/null || true

# -----------------------------------------------------------------------------
# 11. Verify
# -----------------------------------------------------------------------------
echo ""
echo "=== Verification ==="
adb --version
echo "ANDROID_HOME=$ANDROID_HOME"
ls -la "$ANDROID_HOME/build-tools/" 2>/dev/null || echo "WARNING: No build-tools installed"
ls -la "$ANDROID_HOME/platforms/" 2>/dev/null || echo "WARNING: No platforms installed"
echo "Robolectric jar: $(ls -lh "$ROBO_CACHE_DIR/$ROBO_JAR" 2>/dev/null || echo 'NOT FOUND')"
echo "Signing keystore: $(ls -lh "$KEYSTORE" 2>/dev/null || echo 'NOT FOUND')"
echo ""
echo "Android development environment setup complete."
