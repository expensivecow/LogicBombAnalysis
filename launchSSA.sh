#!/bin/bash

ANDROID_JARS_PATH="/home/mike/Dev/Git/Logic-Bomb-Condition-Analysis/android"
JAVA_JARPATH="/home/mike/Dev/Git/Logic-Bomb-Condition-Analysis/jars/soot-3.0.1-jar-with-dependencies.jar:."

APK_FILE=$1

PROCESS_THIS=" -process-dir $APK_FILE" 
SOOT_CLASSPATH="\
"${APK_FILE}":\
"
SOOT_CMD="-android-jars $ANDROID_JARS_PATH \
 -allow-phantom-refs \
 -src-prec apk \
 -keep-line-number \
 -process-multiple-dex
 -f jimple -via-shimple \
 $PROCESS_THIS
"

java \
 -Xmx2g \
 -cp  ${JAVA_JARPATH} \
 AndroidInstrument \
 ${SOOT_CMD}\
