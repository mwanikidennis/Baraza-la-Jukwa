#!/bin/bash
set -e

# 1️⃣ Go to script's directory (where gradlew lives)
cd "$(dirname "$0")"
echo "Project root: $PWD"

# 2️⃣ Set up the JDK and Android SDK
export JAVA_HOME="$PWD/../jdk-17.0.13+11"
export PATH="$JAVA_HOME/bin:$PATH"

export ANDROID_SDK_ROOT="/home/user/.androidsdkroot"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

echo "Java is: $(which java)"
java -version

# 3️⃣ Environment for encoding
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
export GRADLE_OPTS="-Dfile.encoding=UTF-8"

# 4️⃣ Kill daemons & clean cache
./gradlew --stop 2>/dev/null || true
pkill -f GradleDaemon 2>/dev/null || true
rm -rf ~/.gradle/daemon ~/.gradle/caches

# 5️⃣ Run the build
exec ./gradlew clean test --no-daemon --stacktrace
