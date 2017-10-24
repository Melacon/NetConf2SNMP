#!/bin/bash

CONFIG_FILE=/etc/mediatorserver.conf
NETCONF2SNMPFOLDER="/NetConf2SNMP"
MEDIATORFOLDER=$NETCONF2SNMPFOLDER"/Netconf2SNMPMediator"
MEDIATORSERVERFOLDER=$NETCONF2SNMPFOLDER"/MediatorServer"

check_config(){

  if [ -e $CONFIG_FILE ] 
  then
    echo "config file found"
    source $CONFIG_FILE
  else
    echo "no config file found"
    exit 1;
  fi

}

copy_from_source() {

  echo "copying binaries from source"
  cp $home"/source/"$MEDIATORFOLDER"/build/Netconf2SNMPMediator.jar" $home"/bin/"
  cp $home"/source/"$MEDIATORSERVERFOLDER"/bin/MediatorServer.jar" $home"/bin/"
  echo "copying xml ne files from source"
  cp $home"/source/"$MEDIATORFOLDER"/xmlNeModel/*.xml" $home"/nemodel/"
  echo "copying completed"
}
build_source(){
  cd $home"/source/"$NETCONF2SNMPFOLDER
  mvn clean install
}
make_from_source(){
  build_source
  copy_from_source
}
start_server(){

  service mediatorserver start
}
stop_server(){
  service mediatorserver stop
}
stop_mediators(){
  $home"/bin/stop_all.sh"
}
status(){
#TODO
  echo "status"
}



# ------------------------------------------------------------
# ------------ Script Body

echo "************************************************"
echo "*       Control Script for MediatorServer      *"
echo "************************************************"

here=$(pwd)

check_config
echo "HomeDirectory = "$home
echo "------------------------------------------------"
echo ""

case "$1" in

  cpbin)
    copy_from_source
    ;;
  build)
    build_source
    ;;
  make)
    make_from_source
    ;;
  start)
    start_server
    ;;
  stop)
    stop_server
    ;;
  stopall)
    stop_mediators
    stop_server
    ;;
  status)
    status
    ;;
  *)

  echo "Commands:======================"
  echo "cpbin           copy binary files from source"
  echo "build           build sources"
  echo "make            build sources and copy them to bin"
  echo "start           start server"
  echo "stop            stop server"
  echo "stopall         stop all mediators, then the server"
  echo "status          print status"

esac
