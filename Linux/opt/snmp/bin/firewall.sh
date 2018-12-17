#!/bin/bash

PATH=/usr/sbin:/sbin:/usr/bin:/bin
source "/etc/mediatorserver.conf"

cd $home

MEDIATORS_HOME="mediators/"
FWSTATUS_FILE="fwstatus.json"
FOLDERS=`ls -a $MEDIATORS_HOME`

#clear all NAT rules
date
#foreach mediator execute script file
for s in $FOLDERS; do

  s2=$s
  if [[ "$s2" == .* ]]; then
    s2=${s2:1}
  fi
  fn=$MEDIATORS_HOME$s"/"$s2".run"
  echo -n "checking "$fn" ..."
  if [ -e $fn ]; then
    #run NAT rule for this mediator
    ( exec $MEDIATORS_HOME$s"/firewall_add.sh" )
    echo '{"fwactive":true}' > $MEDIATORS_HOME$s"/"$FWSTATUS_FILE
    echo "added"
  else
    ( exec $MEDIATORS_HOME$s"/firewall_remove.sh" )
    echo '{"fwactive":false}' > $MEDIATORS_HOME$s"/"$FWSTATUS_FILE
    echo "removed"
  fi
done


