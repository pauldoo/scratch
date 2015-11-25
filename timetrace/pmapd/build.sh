#!/bin/sh

exec clang++ -Wall -g -DNO_DEBUG_ASSERT -DNO_DEBUG_TRACE -Ofast -march=native -lrt -o pmapd *.cpp

