#!/bin/bash

source /etc/mediatorserver.conf

MEDIATORS_HOME="mediators/"

cd "$home"

create_file(){
  FN=$1
  HOST=$2
  PORT=$3
  echo 'JAVA_JMXCONFIG="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname='$HOST' -Dcom.sun.management.jmxremote.port='$PORT'"' > $FN
}

do_start(){
#get ports for debug from config
  PORTS=($(seq $jmxrangemin 1 $jmxrangemax))
#  echo "ports="$PORTS
#create debug.conf for each mediator
  FOLDERS=`ls $MEDIATORS_HOME`
  i=0
  for f in $FOLDERS; do
    FILE=$MEDIATORS_HOME$f"/debug.conf"
    if [ -e $FILE ]; then
      rm $FILE
    fi
    create_file $FILE $host ${PORTS[i]}
    i=$((i+1))
  done

}
#remove all debug.conf for each mediator
do_stop(){

  FOLDERS=`ls $MEDIATORS_HOME`
  for f in $FOLDERS; do
    FILE=$MEDIATORS_HOME$f"/debug.conf"
    if [ -e $FILE ]; then
      rm $FILE
    fi
  done

}


# sscript body
case "$1" in

  start)
    do_start
    ;;
  stop)
    do_stop
    ;;
  *)

esac

