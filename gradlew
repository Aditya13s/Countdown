#!/bin/sh
# Gradle wrapper script
# Find java
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

APP_HOME=$( cd "${0%[/\\]*}" > /dev/null; pwd -P )
APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

GRADLE_OPTS="${GRADLE_OPTS:-"-Dfile.encoding=UTF-8"}"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVACMD" -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
