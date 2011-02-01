# Convert videos from Dad's camcorder

all: 00002.mp4 00003.mp4 00004.mp4 00005.mp4 00006.mp4 00007.mp4 00008.mp4 00009.mp4 00010.mp4 00011.mp4 00012.mp4 00014.mp4 00014.mp4

%.mp4: %.MTS
	ffmpeg -i $< -deinterlace -vcodec libx264 -vpre hq -acodec libfaac -b 5000k -ab 192k -r 25 $@
	mp4creator -hint=1 $@
	mp4creator -hint=2 $@
	mp4creator -optimize $@

