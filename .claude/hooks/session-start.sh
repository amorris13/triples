#!/bin/bash
set -euo pipefail

# Only run in remote (Claude Code on the web) environments
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

ANDROID_SDK_ROOT="/root/Android/sdk"

##############################################################################
# 1. Fix proxy: Gradle and sdkmanager fail when *.google.com is in
#    nonProxyHosts because it causes direct connections that time out in this
#    container.  We add proxy settings to gradle.properties so Gradle routes
#    all traffic through the proxy.
##############################################################################
PROXY_HOST=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyHost=)\S+' || true)
PROXY_PORT=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyPort=)\S+' || true)

if [ -n "$PROXY_HOST" ] && [ -n "$PROXY_PORT" ]; then
  if ! grep -q "systemProp.http.proxyHost" "$CLAUDE_PROJECT_DIR/gradle.properties" 2>/dev/null; then
    cat >> "$CLAUDE_PROJECT_DIR/gradle.properties" <<GRADLE_PROXY
systemProp.http.proxyHost=${PROXY_HOST}
systemProp.http.proxyPort=${PROXY_PORT}
systemProp.http.nonProxyHosts=localhost|127.0.0.1
systemProp.https.proxyHost=${PROXY_HOST}
systemProp.https.proxyPort=${PROXY_PORT}
GRADLE_PROXY
  fi
fi

##############################################################################
# 2. Android SDK: install platform and build-tools required by the project
##############################################################################
install_sdk_component() {
  local url="$1"
  local dest="$2"

  if [ -d "$dest" ]; then
    return 0
  fi

  local tmpzip
  tmpzip=$(mktemp /tmp/sdk-XXXXXX.zip)
  curl -sL "$url" -o "$tmpzip"
  local tmpdir
  tmpdir=$(mktemp -d /tmp/sdk-extract-XXXXXX)
  unzip -q "$tmpzip" -d "$tmpdir"
  # The zip contains a single directory; move it to the target
  mv "$tmpdir"/*/ "$dest" 2>/dev/null || mv "$tmpdir"/* "$dest"
  rm -rf "$tmpzip" "$tmpdir"
}

install_sdk_component \
  "https://dl.google.com/android/repository/platform-36_r02.zip" \
  "$ANDROID_SDK_ROOT/platforms/android-36"

install_sdk_component \
  "https://dl.google.com/android/repository/build-tools_r35_linux.zip" \
  "$ANDROID_SDK_ROOT/build-tools/35.0.0"

##############################################################################
# 3. local.properties: point Gradle at the SDK
##############################################################################
if [ ! -f "$CLAUDE_PROJECT_DIR/local.properties" ]; then
  echo "sdk.dir=$ANDROID_SDK_ROOT" > "$CLAUDE_PROJECT_DIR/local.properties"
fi

##############################################################################
# 4. Signing keystore: generate a dummy keystore so debug builds succeed
##############################################################################
KEYSTORE="$CLAUDE_PROJECT_DIR/app/keystore.jks"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkey -v \
    -keystore "$KEYSTORE" \
    -alias key \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass password -keypass password \
    -dname "CN=Test, OU=Test, O=Test, L=Test, S=Test, C=US" \
    > /dev/null 2>&1
fi

# Export signing env vars for Gradle
echo 'export RELEASE_STORE_PASSWORD=password' >> "$CLAUDE_ENV_FILE"
echo 'export RELEASE_KEY_ALIAS=key' >> "$CLAUDE_ENV_FILE"
echo 'export RELEASE_KEY_PASSWORD=password' >> "$CLAUDE_ENV_FILE"
echo "export ANDROID_HOME=$ANDROID_SDK_ROOT" >> "$CLAUDE_ENV_FILE"

##############################################################################
# 5. Robolectric: pre-download the android-all-instrumented jar so tests
#    don't need to fetch from Maven at runtime (which fails behind proxy)
##############################################################################
ROBO_DIR="/tmp/robolectric-deps"
ROBO_JAR="$ROBO_DIR/android-all-instrumented-16-robolectric-13921718-i7.jar"
if [ ! -f "$ROBO_JAR" ]; then
  mkdir -p "$ROBO_DIR"
  curl -sL \
    "https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/16-robolectric-13921718-i7/android-all-instrumented-16-robolectric-13921718-i7.jar" \
    -o "$ROBO_JAR"
fi

##############################################################################
# 6. Warm Gradle caches: resolve all dependencies ahead of time
##############################################################################
cd "$CLAUDE_PROJECT_DIR"
./gradlew :app:assembleDebug --quiet 2>/dev/null || true
