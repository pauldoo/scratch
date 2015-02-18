#!/bin/sh

ffmpeg -r 25 -i var/frame_%06d.png -vcodec libx264 -pix_fmt yuv420p -profile high -preset slow -acodec none -b 5000k -r 25 -s 480x270 -aspect 16:9 test.mp4

MP4Box -hint test.mp4
