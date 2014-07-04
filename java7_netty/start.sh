#!/bin/sh

(cd ${BASH_SOURCE%/*}; gradle clean oneJar && java -jar $( ls $(pwd)/build/libs/*standalone.jar ) \
-XX:+UseG1GC \
-Xms4g -Xmx4g \
-XX:-UseAdaptiveSizePolicy \
-XX:NewRatio=1 \
-XX:MaxTenuringThreshold=20 \
# -XX:+PrintGCDetails
)

## Notes:
##+ -XX:+UseG1GC
##+ -Xms4g -Xmx4g : 4 gig heap initial, max
##+ -XX:-UseAdaptiveSizePolicy : disable GC self-tuning for consistency
##+ -XX:NewRatio=1 : young gen will be 1/2 of total heap, great for parallelized apps on multi-core boxes
##+ -XX:MaxTenuringThreshold=20 : for perf, avoid tenuring objects for a while (don't want copy overhead in short tests)
##+ -XX:+PrintGCDetails : enable GC detail printing
