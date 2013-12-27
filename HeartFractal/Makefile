all: image_crop.png

# http://howto.cnet.com/8301-11310_39-57605292-285/create-the-perfect-parallax-wallpaper-in-ios-7/
image_crop.png: image.png
	convert $< -gravity Center -crop 1040x1536+0+0 $@ 

image.png: image.pnm
	convert image.pnm -resize 50% image.png

image.pnm: heart
	./heart > image.pnm

heart: heart.cpp
	g++ -Wall -O3 -ffast-math -o heart heart.cpp

