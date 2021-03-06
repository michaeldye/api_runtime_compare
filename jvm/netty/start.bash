#!/usr/bin/env bash

(cd ${BASH_SOURCE%/*}; ./gradlew oneJar && java -jar -d64 -server \
  -XX:+UseG1GC \
  -Xmn2g -Xms4g -Xmx4g \
  -XX:+AggressiveOpts \
  -XX:-UseAdaptiveSizePolicy \
  $( ls $(pwd)/build/libs/*standalone.jar ))

## Notes:
##+ -XX:+UseG1GC
##+ -Xmn2g -Xms4g -Xmx4g : 2 gig young gen, 4 gig heap total initial and max
##+ -XX:-AggressiveOpts : enable incubating JVM opts
##+ -XX:-UseAdaptiveSizePolicy : disable GC self-tuning for consistency
##+ -XX:+PrintGCDetails : enable GC detail printing
