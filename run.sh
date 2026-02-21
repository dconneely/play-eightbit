#!/bin/sh
cd "$(dirname "$0")"

JAR_FILE="build/libs/eightbit-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Building project..."
    ./gradlew -q --console=plain jar
fi

java --enable-native-access=ALL-UNNAMED -jar "$JAR_FILE"
