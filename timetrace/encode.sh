#!/bin/sh

avconv -r 25 -i var/frame_%06d.png -vcodec libx264 -profile high -preset slow -acodec none -b 5000k -r 25 -s 1920x1080 -aspect 16:9 test.mp4

MP4Box -hint test.mp4
