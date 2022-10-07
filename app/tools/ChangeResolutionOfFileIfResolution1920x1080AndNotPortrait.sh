#!/bin/bash

DIRECTORY=$1
rm -r temp/*
mkdir temp


for filepath in "$DIRECTORY"/*
do
	filename=$(echo "$filepath" | sed "s/.*\///")
	echo $filename
	filesize=$(stat -c%s "$filepath")
	echo $filesize
	if [ $filesize -gt 10000000 ]; then
		filenameWithoutExtension=$(echo "$filepath" | sed -r "s/.+\/(.+)\..+/\1/")
		extension="${filename##*.}"
		echo Extension $extension
		resolution=`ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=p=0 $filepath`
		echo $resolution
		if [[ $resolution == 1920* ]] ;
		then
			echo "Start with 1920"
			modePortrait=`ffprobe -loglevel error -select_streams v:0 -show_entries stream_tags=rotate -of default=nw=1:nk=1 -i $filepath`
			echo $modePortrait
			if [[ -z $modePortrait ]] ;
			then
				echo "Paysage"
				ffmpeg -i $filepath -filter:v "scale='min(1280,iw)':min'(720,ih)':force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2,crop=1280:720" temp/"$filenameWithoutExtension".mp4
				if [[ "$extension" != "mp4" ]] ;
				then
					echo "${filename};${filenameWithoutExtension}.mp4" >> temp/FileChangeResolutionAndExtension
				else 
					echo "$filename" >> temp/FileChangeResolution
				fi
			fi
		fi
	fi
done
