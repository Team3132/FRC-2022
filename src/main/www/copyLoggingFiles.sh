#!/bin/bash
# Copies files from a logging USB flash drive to the local computer.
# It saves them in a folder after the match number and also by date.
#
# Must be run from the mounted USB drive.
#
# Only tested on Linux.

set -u # Abort on unset variables.
shopt -s extglob # enable the !(blah) syntax in shell.

DEST=~/Desktop/frc/logs

list_mounts() {
    FILE=$1
    df -h|grep ^/dev/|awk '{print $1}' > $FILE
}

check_running_from_USB() {
    # This in enforced so that we know exactly where to copy the files from
    # Example output from df -h .
    # user@host:/media/user/3132_logging/scripts$ df -h .
    # Filesystem      Size  Used Avail Use% Mounted on
    # /dev/sdb1        15G   43M   14G   1% /media/user/3132_logging
    #
    # As this script should be in the top directory of the flash drive, we
    # expect that:
    #  dirname($0) == the name of the mount point in the df of dirname($0)
    ABS_PATH=$(realpath $0)
    DIR=$(dirname $ABS_PATH)
    DF_DIR=$(df -h $DIR|tail -1)
    if df -h $DIR | tail -1 | grep -q "$DIR\$" ; then
    	echo "Script appears to be running from USB flash drive, continuing..."
    	echo
    else
    	echo
    	echo "ERROR: Script doesn't appear to be running off the root of a mounted USB flash drive, aborting"
    	echo "Please run the script from the flash drive. DIR=$DIR"
    	echo
    	exit 1
    fi 
}

main() {
	check_running_from_USB
	# Variable DIR now contains the partition to copy from.
	
	set -e
	mkdir -p $DEST
	
    echo "Copying files from $DIR to $DEST"
    # Skip the lost+found directory as that will cause an error due to permissions.
    cp -r $DIR/!(lost+found) "$DEST"/  # Uses extglob syntax
	set +e
	echo
	echo "Copying completed successfully, press any key to continue."
	echo
	echo "Run the webserver by running script $DEST/startWebserver.sh"
    echo "Then the files should be available at http://localhost:8000/"
    read dummy
}

main