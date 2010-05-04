all: image.png image.jpg

image.pnm: buddhabrot kernel.cl
	time ./buddhabrot > image.pnm

OBJS := main.o
CXXFLAGS := -Wall -O3 -g

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
