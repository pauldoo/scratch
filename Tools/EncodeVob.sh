#!/bin/zsh

nice ffmpeg -i ${1} -filter:v yadif -c:v libx264 -profile:v high -preset fast -crf 24 -c:a libfaac -ab 128k ${1}.mp4
mp4creator -hint=1 ${1}.mp4
mp4creator -hint=2 ${1}.mp4
mp4creator -optimize ${1}.mp4

