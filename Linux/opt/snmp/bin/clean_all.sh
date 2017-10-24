#!/bin/bash

source /etc/mediatorserver.conf

MEDIATORS_HOME="mediators/"

cd "$home"

JPS="jps -m"

FOLDERS=`ls $MEDIATORS_HOME`

echo "searching in "$FOLDERS

#foreach mediator
for f in $FOLDERS; do

  runfile=$MEDIATORS_HOME$f"/"$f".run"
  lckfile=$MEDIATORS_HOME$f"/"$f".lck"
  pidfile=$MEDIATORS_HOME$f"/"$f".pid"

  filter=$f"/"$f".config"

  x=(`$JPS | grep $filter`)
#  echo "result="${x[@]}
  if [ ${#x[@]} -gt 0 ]
  then  #process exists
    echo "writing pidfile for "$f
#    a=($x)
    echo ${x[0]} > $pidfile
  else
    echo "deleting pid and lock file for "$f
    if [ -e $lckfile ]; then
      rm $lckfile
    fi
    if [ -e $pidfile ]; then
      rm $pidfile
    fi
  fi
done
