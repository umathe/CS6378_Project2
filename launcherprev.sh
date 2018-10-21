#!/bin/bash

# Change this to your netid
netid=rxs131130

# Root directory of your project
PROJDIR=/home/010/r/rx/rxs131130/TestProj

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/Desktop/launch/SampleInput.txt

# Directory your java classes are in
BINDIR=$PROJDIR

# Your main project class
PROG=Project_Sockets

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
	
	# minal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $p; exec bash" &

	osascript -e 'tell app "Terminal"
	do script "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no '$netid@$host' java -cp '$BINDIR' '$PROG' '$n'; '$SHELL'"
	end tell'

        n=$(( n + 1 ))
    done
)
