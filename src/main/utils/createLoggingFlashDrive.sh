#!/bin/bash
# Creates logging USB drive by reformatting it and copying the needed
# files on to it. Use at your own risk.
#
# See the README.md in the www directory.
#
# WARNING: This has the potential to erase all the data on your
# computer if you aren't paying attention. Do not run unless
# you know what you are doing. Chat with Mark or Rex before using.
#
# Only tested on Linux.

set -u # Abort on unset variables.

list_mounts() {
    FILE=$1
    df -h|grep ^/dev/|awk '{print $1}' > $FILE
}

# New partition will be in the variable $PARTITION. Exits on failure.
get_usb_partition() {
	echo "Please eject or umount the USB flash drive if mounted and press enter"
	read dummy
    BEFORE_MOUNT=/tmp/before.mnts
    list_mounts $BEFORE_MOUNT
    echo "Please insert or mount USB flash drive to format and press enter"
    read dummy
    echo "Waiting for the OS to mount new partitions....."
    sleep 5
    AFTER_MOUNT=/tmp/after.mnts
    list_mounts $AFTER_MOUNT
    NEW_PARTITIONS=/tmp/new.mnts
    
    diff $BEFORE_MOUNT $AFTER_MOUNT | grep '^>' |awk '{print $2}' > $NEW_PARTITIONS 
    ROWS=$(wc -l $NEW_PARTITIONS | awk '{print $1}')
    if [ $ROWS -gt 1 ]; then
        echo "Too many new partitions found, aborting"
        cat $NEW_PARTITIONS
        exit 1
    fi
    if [ $ROWS -lt 1 ]; then
        echo "No new partition was detected, aborting"
        cat $AFTER_MOUNT
        exit 2
    fi
    PARTITION=$(cat $NEW_PARTITIONS)
    MOUNT_POINT=$(mount | grep $PARTITION |sed -e 's/^.* on \([^ ]*\).*/\1/') 
    echo
    echo "Detected new partition $PARTITION, mounted on $MOUNT_POINT"
    # Sanity check that it's mounted in an expected location.
    # Extract the first part of the path and compare it with a list.
    FIRST_DIR=$(echo $MOUNT_POINT | sed -e 's@^\(/[^/]*\).*@\1@')
    if [ "$FIRST_DIR" != "/media" ] && [ "$FIRST_DIR" != "/mnt" ]&& [ "$FIRST_DIR" != "/tmp" ]; then
    	echo "Unexpected mount point $FIRST_DIR, giving up for safety"
    	exit 3
    fi 
    echo
    echo "Is this the correct partition/filesystem [y/N]? Answering y will destroy all data on this device."
    read CONTINUE
    echo
    if [ "$CONTINUE" != "y" ]; then
    	echo "Aborted"
    	exit 4
    fi
}

check_partition_is_ext4() {
	PARTITION=$1
	FORMAT=$(mount |grep $PARTITION|sed -e 's/^.* type \([^ ]*\).*/\1/')
	echo "Partition $PARTITION has format $FORMAT"
	echo
	if [ "$FORMAT" != "ext4" ]; then
		echo "Error: Format should be ext4, please run the following commands and try again"
		echo
		echo "  sudo umount $PARTITION"
		echo "  sudo mkfs.ext4 $PARTITION"
	    echo
        echo "WARNING: If the wrong partition is specified, you will likely lose data!"
        exit 5
    fi
}

reformat() {
	PARTITION=$1
	MOUNT_POINT=$2
	echo
	set -ex # abort on error and enable logging
	sudo umount $PARTITION
    # This prompts if the user wants to continue.
	sudo mkfs.ext4 $PARTITION -L "3132_logging"
	set +xe
	
	sudo mkdir -p $MOUNT_POINT
	sudo mount $PARTITION $MOUNT_POINT
	sudo chmod a+w $MOUNT_POINT
		
	check_partition_is_ext4 $PARTITION
	
	echo "$PARTITION is now formatted as ext4 and mounted on $MOUNT_POINT"
}

copy_files() {
	MOUNT_POINT=$1
	# Check that we really do have a mount point.
	if [ "$MOUNT_POINT" = "" ]; then
	    echo "Need to specify a mount point"
	    exit 7
    fi
    # Detect the location of the files based on where this script is.
    SCRIPT_LOCATION=$(dirname $0)
    FILES_LOCATION="${SCRIPT_LOCATION}/../www/"
    set -e
    echo
    echo "Copying files to flash drive"
    cp -r "${FILES_LOCATION}"/* ${MOUNT_POINT}
    # Ensure all shell scripts are executable.
    chmod a+x ${MOUNT_POINT}/*.sh
    echo "Changing ownership of files to lvuser."
    # On the roborio, this is id 500.
    sudo chown -R 500:500 "${MOUNT_POINT}"/*
    set +e
	echo
    echo "File/directory listing:"
    (cd ${MOUNT_POINT} && find * -type f 2>/dev/null)
    echo
}

main() {
    # Execution starts here.
    
    get_usb_partition
    # Partition now in variable PARTITION.

    # Abort if it's not ext4. If it's already ext4, then it's likely
    # that this is already a valid logging USB flash drive.
    # We may remove this check if it proves to be annoying.
    #check_partition_is_ext4 $PARTITION

    # Partition is already ext4, let's reformat to clear it.
    MOUNT_POINT=/tmp/logging
    reformat ${PARTITION} ${MOUNT_POINT}
    
    # Copy all files across to set it up correctly. The files should
    # already be correctly laid out in the src/www/ directory.
    copy_files ${MOUNT_POINT}

    # Record who created this image and when
    echo "Created by $USER on $(hostname) on $(date)" > ${MOUNT_POINT}/CreatedBy.txt

    # Ensure that copy completes by waiting for sync -f to finish. It will wait for all copies.
    sync -f ${MOUNT_POINT}
    echo "umounting..."
    sudo umount ${MOUNT_POINT}
    echo
    echo "Copying complete, you may now remove the USB flash drive and use it in the robot"
}

main
