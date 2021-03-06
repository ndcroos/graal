#!/bin/sh
# add all libraries to the build.xml file

./scripts/wget-dep.sh

LIBS=$(ls -l ./lib | head -n 2 | tail -n 1 | perl -n -e '/.* (.*)/ && print "lib/$1"')
LIBS=${LIBS}$(ls -l ./lib | tail -n +2 | perl -n -e '/.* (.*)/ && print ":lib/$1"')

cat scripts/build.xml.prefix > build.xml
echo "<property name=\"lib\" value=\"${LIBS}\" />" >> build.xml
cat scripts/build.xml.suffix >> build.xml
