#!/bin/bash

source /etc/mediatorserver.conf

MEDIATORS_HOME="mediators/"

cd "$home"

FOLDERS=`ls $MEDIATORS_HOME`

#clear all pidfiles
#TODO
#LOGFILE="/var/log/mediators.log"

echo "searching in "$FOLDERS

#foreach mediator execute script file
for f in $FOLDERS; do

  start_script=$MEDIATORS_HOME$f"/start.sh"
  runfile=$MEDIATORS_HOME$f"/"$f".run"
  lckfile=$MEDIATORS_HOME$f"/"$f".lck"

  if [ -e $start_script ]; then  #startscript exists
    if [ -e $runfile ]; then #do autorun
      #execute script
      echo "starting "$f
      sh $start_script > /dev/null 2>&1 &
    else
      echo $f" is not for autorun"
    fi
  fi
done
