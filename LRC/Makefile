# Long Range Compressor (LRC)
# Copyright (c) 2006, 2012 Paul Richards <paul.richards@gmail.com>
#
# Permission to use, copy, modify, and/or distribute this software for any
# purpose with or without fee is hereby granted, provided that the above
# copyright notice and this permission notice appear in all copies.
#
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
# WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
# ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
# WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
# ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
# OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.


objects := $(patsubst %.cpp, %.o, $(wildcard *.cpp))
deps := $(patsubst %.cpp, %.dep, $(wildcard *.cpp))

CXXFLAGS = -Wall -O2 -g -pipe
CPPFLAGS =
LIBS = -lboost_program_options -lcrypto++

all: External.h.gch lrc

clean:
	rm -f lrc *.o *.gch

lrc: $(objects)
	$(CXX) $(CXXFLAGS) -o $@ $(objects) $(LIBS)

%.dep: %.cpp
	$(CXX) -MM $< > $@ $(CPPFLAGS)

%.h.gch: %.h
	$(CXX) $(CXXFLAGS) -c -o $@ $< $(CPPFLAGS)

%.o: %.cpp %.dep
	$(CXX) $(CXXFLAGS) -c -o $@ $< $(CPPFLAGS)

include $(deps)

