#!/bin/sh

gradle clean oneJar && java -Xms4g -Xmx4g -XX:-UseAdaptiveSizePolicy -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:NewRatio=1 -XX:SurvivorRatio=1 -XX:MaxTenuringThreshold=20 -XX:+UseG1GC -jar $( ls ./build/libs/*standalone.jar )
