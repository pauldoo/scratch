#/bin/zsh

# Use "lsdvd /dev/disk1" to figure out the relevant titles.
# Then run ./RipVobs.sh <dvd-name> <first-title> <last-title>

for c in `seq $2 $3`; do
    mplayer dvd://${c} -dvd-device /dev/disk1 -dumpstream -dumpfile vobs/dvd_${1}_title_${c}.vob
done

