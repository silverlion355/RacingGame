#!/bin/sh

# Gradle start up script for POSIX (Linux / macOS / Git Bash on Windows)
# 简化且正确的包装脚本：定位 JDK 并启动 gradle-wrapper.jar

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" 1>&2
        exit 1
    fi
else
    JAVACMD="java"
    command -v java >/dev/null 2>&1 || {
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." 1>&2
        exit 1
    }
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
