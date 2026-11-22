#!/bin/sh
# Maven launcher using project-local JDK and Maven binaries.
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME="$SCRIPT_DIR/libraries/java/jdk/jdk-27.0.1.1"
export MAVEN_HOME="$SCRIPT_DIR/libraries/maven/apache-maven-3.10.1"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

if [ ! -f "$JAVA_HOME/bin/java" ]; then
    echo "[ERROR] JDK not found at $JAVA_HOME"
    exit 1
fi

if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
    echo "[ERROR] Maven not found at $MAVEN_HOME"
    exit 1
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
