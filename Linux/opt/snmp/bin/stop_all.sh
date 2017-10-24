#!/bin/bash

source /etc/mediatorserver.conf

MEDIATORS_HOME="mediators/"

cd "$home"

FOLDERS=`ls $MEDIATORS_HOME`

#clear all pidfiles
#TODO

#echo "searching in "$FOLDERS

#foreach mediator execute script file
for f in $FOLDERS; do

  stop_script=$MEDIATORS_HOME$f"/stop.sh"
  runfile=$MEDIATORS_HOME$f"/"$f".run"
  lckfile=$MEDIATORS_HOME$f"/"$f".lck"

  if [ -e $stop_script ]; then  #startscript exists
    #execute script
    sh $stop_script > /dev/null 2>&1 &
  fi
done 
