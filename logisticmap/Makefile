all: output.png

clean:
	rm -f logisticmap output.*

output.png: output.pnm
	convert $< $@

output.pnm: logisticmap
	time ./logisticmap > output.pnm

logisticmap: logisticmap.cpp
	c++ -std=c++11 -Wall -Ofast -march=native -o $@ $<

