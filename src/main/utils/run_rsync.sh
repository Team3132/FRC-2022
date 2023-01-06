#!/bin/bash
#
# Team 3132 run_rsync.sh
#
# Script to pull logging files from the robot and put them into a
# local Google Drive folder for syncing later when the laptop is
# connected to the internet.
#
# This allows graphs and logs to be viewed by anyone anywhere in
# the world with an internet connection to help debug robot issues.
#
# This script needs to be run on the drivers station laptop whenever
# the laptop starts.
# rsync -e ssh -vahL "/cygdrive/c/ROBOT_USERs/TDU-3132-DS-COMP1/Google Drive/logging" sci.frc@ash.science.mq.edu.au:/home/sci.frc/html/

ROBOT_HOST="10.31.32.2"
ROBOT_USER="lvuser"
ROBOT_DIR="/media/sda1/*"

#LOCAL_DIR="/cygdrive/c/ROBOT_USERs/TDU-3132-DS-COMP1/Dropbox" # May change the file name later
LOCAL_DIR="${HOME}/logging/" # May change the file name later

WEBSERVER_HOST="ash.science.mq.edu.au"
WEBSERVER_USER="sci.frc"
WEBSERVER_DIR="/home/sci.frc/html/logging"

SLEEP_TIME="2m"
LOG_FILE="${HOME}/run_rsync.log"
RSYNC="/usr/bin/rsync"

cd ${HOME}
export PATH=/usr/bin:$PATH
echo "run_rsync started, check the run_rsync.log file for output"

mv ${LOG_FILE}.1 ${LOG_FILE}.2
mv ${LOG_FILE} ${LOG_FILE}.1

exec &>> $LOG_FILE 

while /bin/true; do
  echo "$(date) Running rsync to pull from the robot, this may fail if this laptop isn't connected to the robot"
  ${RSYNC} -vah -e ssh "${ROBOT_USER}@${ROBOT_HOST}:${ROBOT_DIR}" "${LOCAL_DIR}"
  echo "rsync completed with error code $?"

  echo "$(date) Running rsync to push to the webserver, this may fail if this laptop isn't connected to the internet"
  ${RSYNC} -vah -e ssh "${LOCAL_DIR}" "${WEBSERVER_USER}@${WEBSERVER_HOST}:${WEBSERVER_DIR}" 
  echo "rsync completed with error code $?"

  echo "Sleeping for ${SLEEP_TIME}"
  sleep ${SLEEP_TIME}
done
