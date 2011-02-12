all: image.png

image.png: image.pnm
	convert image.pnm -resize 50% image.png

image.pnm: heart
	./heart > image.pnm

heart: heart.cpp
	g++ -Wall -O3 -ffast-math -o heart heart.cpp

