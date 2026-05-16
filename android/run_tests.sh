#!/bin/bash
set -e

# 1️⃣ Go to script's directory (where gradlew lives)
cd "$(dirname "$0")"
# Use quotes for PWD to handle spaces
echo "Project root: $PWD"

# 2️⃣ Set up the JDK
# We strictly want Java 17. If current JAVA_HOME is not 17 or invalid, we search.
CURRENT_JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1 || echo "0")

if [ "$CURRENT_JAVA_VER" != "17" ] || [ ! -d "$JAVA_HOME" ]; then
    echo "Current Java is v$CURRENT_JAVA_VER (not 17) or JAVA_HOME is invalid. Searching for JDK 17..."
    unset JAVA_HOME
    
    # Use quotes for the parent directory path
    PARENT_DIR="$(cd .. && pwd)"
    LOCAL_JDK="$PARENT_DIR/jdk-17.0.13+11"
    
    # If exact version not found, look for any jdk-17 folder in root
    if [ ! -d "$LOCAL_JDK" ]; then
        MATCH=$(ls -d "$PARENT_DIR"/jdk-17* 2>/dev/null | head -n 1)
        if [ -n "$MATCH" ]; then
            LOCAL_JDK="$MATCH"
        fi
    fi

    # Use HOME instead of USER as it's more reliable in MINGW64
    USER_HOME_UNIX=$(cygpath -u "$HOME")
    
    # Try discovered locations and common Windows paths
    for possible_jdk in \
        "$LOCAL_JDK" \
        "$USER_HOME_UNIX/AppData/Local/Programs/Eclipse Adoptium/jdk-17.0.19.10-hotspot" \
        "$USER_HOME_UNIX/AppData/Local/Programs/Eclipse Adoptium/jdk-17"* \
        "$USER_HOME_UNIX/AppData/Roaming/Antigravity/User/globalStorage/pleiades.java-extension-pack-jdk/java/17" \
        "/c/Program Files/Java/jdk-17"* \
        "/c/Program Files/Eclipse Adoptium/jdk-17"*; do
        if [ -d "$possible_jdk" ]; then
            export JAVA_HOME="$possible_jdk"
            break
        fi
    done
fi

if [ -n "$JAVA_HOME" ]; then
    # Convert to Unix path if it looks like Windows
    if [[ "$JAVA_HOME" == [a-zA-Z]:* ]]; then
        JAVA_HOME=$(cygpath -u "$JAVA_HOME")
    fi
    # Put our preferred Java at the FRONT of the PATH
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 3️⃣ Set up the Android SDK
# Priority: 1. Existing ANDROID_HOME, 2. User's standard Windows SDK path
if [ -z "$ANDROID_HOME" ]; then
    # Use the reliable Unix home path we calculated earlier
    WINDOWS_SDK="$USER_HOME_UNIX/AppData/Local/Android/Sdk"
    if [ -d "$WINDOWS_SDK" ]; then
        export ANDROID_HOME="$WINDOWS_SDK"
    else
        # Fallback to legacy path from original script if it exists
        export ANDROID_HOME="/home/user/.androidsdkroot"
    fi
fi

# Convert to Unix path if it looks like Windows
if [[ "$ANDROID_HOME" == [a-zA-Z]:* ]]; then
    ANDROID_HOME=$(cygpath -u "$ANDROID_HOME")
fi

export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# 4️⃣ Verify tools before proceeding
if ! command -v java &> /dev/null; then
    echo "ERROR: java command not found. Please ensure JDK 17 is installed and JAVA_HOME is set."
    echo "Current PATH: $PATH"
    exit 1
fi

echo "Java is: $(which java)"
java -version

# 5️⃣ Environment for encoding
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
export GRADLE_OPTS="-Dfile.encoding=UTF-8"

# 6️⃣ Kill daemons & clean cache
./gradlew --stop 2>/dev/null || true
# Note: pkill might not be available on all MINGW64 setups, so we ignore errors
pkill -f GradleDaemon 2>/dev/null || true

# 7️⃣ Run the build
# Wrap gradlew call in quotes
exec ./gradlew clean test --no-daemon --stacktrace

