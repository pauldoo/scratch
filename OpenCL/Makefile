#ATISTREAMSDKROOT ?= /home/pauldoo/ati-stream-sdk-v2.0-beta2-lnx64
#LD_LIBRARY_PATH := $(LD_LIBRARY_PATH):$(ATISTREAMSDKROOT)/lib/x86_64
#export LD_LIBRARY_PATH

OBJS := main.o common.o warpvanilla.o warpvanillaf.o warpopencl.o warpopencl2.o warpvanilla2.o warpopenmp.o
CFLAGS := -std=c99 -Wall -O3 --fast-math -fopenmp

output.txt: test WarpOpenCL.cl
	./test | tee $@

test: $(OBJS)
	gcc -Wall -lm -lgomp -framework OpenCL -o $@ $(OBJS)

-include $(OBJS:.o=.d)

%.o: %.c
	gcc -c $(CFLAGS) $*.c -o $*.o
	gcc -MM $(CFLAGS) $*.c > $*.d

clean:
	rm -f output.txt test *.o *.d

