#!/bin/zsh

for i in `seq 10000 10999`; do
    A=`perl -e "print sin(($i / 1000.0) * 2 * 3.1415) * 0.25 - 1.0;"`
    B=`perl -e "print cos(($i / 1000.0) * 2 * 3.1415) * 0.25;"`

    echo $i $A $B
    ./buddhabrot -2.5 -2.5 $A $B 2.5 2.5 $A $B > frameDa_$i.pnm
done

for i in `seq 10000 10999`; do
    A=`perl -e "print sin(($i / 1000.0) * 2 * 3.1415) * 0.26 - 1.0;"`
    B=`perl -e "print cos(($i / 1000.0) * 2 * 3.1415) * 0.26;"`

    echo $i $A $B
    ./buddhabrot -2.5 -2.5 $A $B 2.5 2.5 $A $B > frameDb_$i.pnm
done

for i in `seq 10000 10999`; do
    A=`perl -e "print sin(($i / 1000.0) * 2 * 3.1415) * 0.24 - 1.0;"`
    B=`perl -e "print cos(($i / 1000.0) * 2 * 3.1415) * 0.24;"`

    echo $i $A $B
    ./buddhabrot -2.5 -2.5 $A $B 2.5 2.5 $A $B > frameDc_$i.pnm
done

