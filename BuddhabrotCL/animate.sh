#!/bin/zsh

for i in `seq 10000 10999`; do
    A=`perl -e "print sin(($i / 1000.0) * 2 * 3.1415) * 0.5 - 0.5;"`
    B=`perl -e "print cos(($i / 1000.0) * 2 * 3.1415) * 0.5;"`

    echo $i $A $B
    ./buddhabrot -2.5 -2.5 $A $B 2.5 2.5 $A $B > frameC_$i.pnm
done


