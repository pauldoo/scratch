#!/bin/bash

echo "RUNNING THROUGH DEBUG WRAPPER"

ulimit -c unlimited

strace $(dirname $0)/pmapd $*

exit $?

