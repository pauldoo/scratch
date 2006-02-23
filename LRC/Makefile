objects := $(patsubst %.cpp, %.o, $(wildcard *.cpp))
deps := $(patsubst %.cpp, %.dep, $(wildcard *.cpp))

CXXFLAGS = -Wall -O2 -g -pipe
CPPFLAGS =
CXX = g++-3.4

all: External.h.gch lrc

clean:
	rm -f lrc *.o *.gch

lrc: $(objects)
	$(CXX) $(CXXFLAGS) -o $@ $(objects) $(LIBS)

%.dep: %.cpp
	$(CXX) -MM $< > $@

%.h.gch: %.h
	$(CXX) $(CXXFLAGS) -c -o $@ $< $(CPPFLAGS)

%.o: %.cpp %.dep
	$(CXX) $(CXXFLAGS) -c -o $@ $<

include $(deps)
