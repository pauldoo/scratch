all: image.png

image.pnm: buddhabrot kernel.cl
	./buddhabrot > image.pnm

OBJS := main.o
CXXFLAGS := -Wall -Os

buddhabrot: $(OBJS)
	g++ -Wall -framework OpenCL -o $@ $(OBJS)

-include $(OBJS:.o=.d)

%.o: %.cpp
	g++ -c $(CXXFLAGS) $*.cpp -o $*.o
	g++ -MM $(CXXFLAGS) $*.cpp > $*.d

%.png: %.pnm
	convert $< $@

clean:
	rm -f image.pnm buddhabrot *.o *.d
