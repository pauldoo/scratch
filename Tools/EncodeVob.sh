#!/bin/zsh

nice ffmpeg -i ${1} -filter:v yadif,scale=768:576,crop=736:560:16:8,deshake -c:v libx264 -profile:v high -preset medium -crf 23 -c:a libfaac -ab 128k ${1}.mp4
mp4creator -hint=1 ${1}.mp4
mp4creator -hint=2 ${1}.mp4
mp4creator -optimize ${1}.mp4

