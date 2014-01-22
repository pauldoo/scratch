all: foobar-clang foobar-gcc
	sysctl -n machdep.cpu.brand_string
	./foobar-clang
	./foobar-gcc

foobar-clang: main.cpp
	c++ -Wall -Ofast -o $@.s -S $<
	c++ -Wall -Ofast -o $@ $<

foobar-gcc: main.cpp
	g++ -Wall -Ofast -o $@.s -S $<
	g++ -Wall -Ofast -o $@ $<


