#!/usr/bin/env bash

set -e
set -x

INPUT_OPTIONS="-framerate 30 -f image2 -pattern_type glob"

OUTPUT_OPTIONS="-pix_fmt yuv420p -c:a none -c:v libvpx-vp9 -crf 23 -f webm"

PASSLOG=$(mktemp)

ffmpeg $INPUT_OPTIONS -i "./output/frame_*.png" $OUTPUT_OPTIONS -pass 1 -passlogfile $PASSLOG /dev/null

ffmpeg $INPUT_OPTIONS -i "./output/frame_*.png" $OUTPUT_OPTIONS -pass 2 -passlogfile $PASSLOG render.webm

