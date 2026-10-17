#!/bin/sh
cd "$(dirname "$0")"

JAR_FILE="build/libs/eightbit-1.0.0-SNAPSHOT.jar"

# Gradle rebuilds the jar only if the code has changed since it was last built.
./gradlew -q --console=plain jar || exit 1

java --enable-native-access=ALL-UNNAMED -jar "$JAR_FILE" "$@"
