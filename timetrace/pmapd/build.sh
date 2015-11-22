#!/bin/sh

exec c++ -Wall -g -DNO_DEBUG_ASSERT -DNO_DEBUG_TRACE -flto -Ofast -march=native -o pmapd *.cpp

