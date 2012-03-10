all: buddha.png juddha.png

buddha.pnm: buddhabrot kernel.cl
	time ./buddhabrot 0.0 0.0 -2.5 -1.5 0.0 0.0 1.5 1.5 > $@
juddha.pnm: buddhabrot kernel.cl
	time ./buddhabrot -2.0 -2.0 -0.4 0.6 2.0 2.0 -0.4 0.6 > $@

OBJS := main.o
CXXFLAGS := -Wall -Os -g

buddhabrot: $(OBJS)
	g++ -Wall -framework OpenCL -o $@ $(OBJS)

-include $(OBJS:.o=.d)

%.o: %.cpp
	g++ -c $(CXXFLAGS) $*.cpp -o $*.o
	g++ -MM $(CXXFLAGS) $*.cpp > $*.d

%.png: %.pnm
	convert $< $@

%.jpg: %.pnm
	convert $< $@

clean:
	rm -f image.pnm buddhabrot *.o *.d
