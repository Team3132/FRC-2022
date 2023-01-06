# Logging Flash Drive

This directory contains the files needed to view the logs created by the robot.
These logs are useful for diagnosing why a robot wasn't performing correctly.
Often the team isn't in a position where we can easily reproduce the problem,
so we want to know what the robot was 'thinking' at the time it was doing something weird.

The logging code logs text messages and also numeric values that are able to be charted.

These supporting files are put on a USB flash drive by the script `src/utils/createLoggingUSB.sh`
which formats the flash drive and copies the files.

Once the files have been copied, the USB flash drive can be inserted in a robot.

It is recommended that the flash drives are swapped every match with a newly created one and
the files are copied off using the script `copyLoggingFiles.sh` script. This way the data from
the last run can be analysed after each match. The flash drives should be recreated each
time so that there aren't lots of logs to search amongst to find the match you're looking for.

## [Re]imaging the USB flash drive

### Prerequisites

Requires a Linux machine (sorry, OSX and Windows aren't supported). Windows doesn't support ext4.

### Instructions

WARNING: This will delete all data off the flash drive. See the Copying data section below if you
want to copy it off first.
Open a terminal and type:

```
  # cd to the location of the util files, eg
  cd ~/git/Comp2018Robot/src/utils
  # Run the script to format the flash drive and copy the files across. 
  ./createLoggingFlashDrive.sh
```

Then insert the USB flash drive into the USB port of the RoboRio. Restart the robot code if it's
already running.

## Viewing the logs and charts on a running robot

### Prerequisites

Requires a Linux machine (sorry, OSX and Windows aren't supported). Windows doesn't support ext4.

### Instructions

The robot runs it's own webserver on http://roborio-3132-frc.local:5800/ which can be used to
view the [Latest_chart.html](http://roborio-3132-frc.local:5800/Latest_chart.html) and the
[Latest_log.txt](http://roborio-3132-frc.local:5800/Latest_log.txt). 

## Viewing the logs directly on the USB flash drive

### Start the local web server.

At the end of a match the flash drive can be removed and plugged into a Linux laptop for viewing.

```
  # cd to the location of the mounted flash drive.
  cd /media/*/3132_logging
  # Start the local webserver
  ./startWebserver.sh
```

If it fails, it maybe because another webserver is already running. You will need to kill that
first by using Control+C on that terminal window.

Then you can view the logs etc by going to http://localhost:8000/

## Saving the logs from a robot

At the end of each match in competition, the flash drive should be swapped with a newly imaged
flash drive and the old one copied onto someones laptop so that the data collected from the match
can be checked.

This allows the team to compare what the drive team saw with what the robot was doing and why it was doing it.

### Copying the logs off the flash drive and onto the laptop

The script to do the copying is included on the flash drive.

You will need a Linux laptop as Windows doesn't support ext4.

```
  # cd to the location of the mounted flash drive.
  cd /media/*/3132_logging
  # Copy the logging files to the local computer.
  ./copyLoggingFiles.sh
```

The files will have been put in the directory ~/Desktop/frc/logs/

### Viewing the logs on the laptop

Viewing the logs is much the same as viewing the files on the flash drive.

```
  cd ~/Desktop/frc/logs
  ./startWebserver.sh
```

Then you can view the logs etc by going to http://localhost:8000/

### Resetting the flash drive

Once the files have been safely copied off, the drive should be reimaged. See the [Re]imaging the USB flash drive section.
