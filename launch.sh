#!/bin/bash

ANDROID_JARS_PATH="/home/mike/Dev/Git/Logic-Bomb-Condition-Analysis/android"
JAVA_JARPATH="/home/mike/Dev/Git/Logic-Bomb-Condition-Analysis/jars/soot-3.0.1-jar-with-dependencies.jar"

APK_FILE=$1

PROCESS_THIS=" -process-dir $APK_FILE" 
SOOT_CLASSPATH="\
"${APK_FILE}":\
"
SOOT_CMD="-android-jars $ANDROID_JARS_PATH \
 -allow-phantom-refs \
 -src-prec apk \
 -keep-line-number \
 -process-multiple-dex \
 -p jb.ne enabled:false \
 -p jb use-original-names:true \
 -p jb.lp unsplit-original-locals:true \
 -p jb.lns only-stack-locals:true \
 -p jb.ulp enabled:false \
 -p jb.a enabled:false \
 -p jb.ls enabled:true \
 -f none \
 $PROCESS_THIS
"

java \
 -Xmx8g \
 -cp  ${JAVA_JARPATH} \
 /analysis/src/main/java/org/mike/logicbomb/analysis/Analysis.java \
 ${SOOT_CMD}\
