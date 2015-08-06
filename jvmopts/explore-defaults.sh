#/bin/bash -e

export COMMON_ARGS="-XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -version"
java -server $COMMON_ARGS > base.txt
java -server -XX:+UseConcMarkSweepGC $COMMON_ARGS > cms.txt
java -server -XX:+UseG1GC $COMMON_ARGS > g1gc.txt
java -server -XX:+AggressiveOpts $COMMON_ARGS > aggressiveopts.txt
java -server -XX:+AggressiveHeap $COMMON_ARGS > aggressiveheap.txt
java -server -XX:+UseParallelGC $COMMON_ARGS > parallel.txt
java -server -XX:+UseParallelOldGC $COMMON_ARGS > parallelold.txt

