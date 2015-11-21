#!/bin/sh

exec c++ -Wall -g -flto -Ofast -march=native -o pmapd *.cpp

