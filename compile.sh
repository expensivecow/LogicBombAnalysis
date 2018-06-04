#!/bin/bash

SOOT_JAR_PATH=".:jars/soot-3.0.1-jar-with-dependencies.jar"

COMPILE=$1

javac \
 -g \
 -cp $SOOT_JAR_PATH \
 $COMPILE