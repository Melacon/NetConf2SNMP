#!/bin/bash

CONFIG_FILE=/etc/mediatorserver.conf
NETCONF2SNMPFOLDER="/Netconf2SNMP/Netconf2SNMP"
MEDIATORFOLDER=$NETCONF2SNMPFOLDER"/Netconf2SNMPMediator"
MEDIATORSERVERFOLDER=$NETCONF2SNMPFOLDER"/MediatorServer"
XMLCREATORFOLDER=$NETCONF2SNMPFOLDER"/SNMPXmlCreator"

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
copy_xml(){
  echo "copying xml ne files from source"
  cp $home"/source/"$MEDIATORFOLDER"/xmlNeModel/DVM_MWCore12_BasicAir.xml" $home"/nemodel/"

}
copy_from_source() {

  echo "copying binaries from source"
  cp $home"/source/"$MEDIATORFOLDER"/build/Netconf2SNMPMediator.jar" $home"/bin/"
  cp $home"/source/"$MEDIATORSERVERFOLDER"/bin/MediatorServer.jar" $home"/bin/"
  copy_xml
  echo "copying completed"
}
checkout(){
  cd $home"/source/"$NETCONF2SNMPFOLDER
  git pull
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
  echo "starting mediator server"
  service mediatorserver start
}
stop_server(){
  echo "stopping mediator server"
  service mediatorserver stop
}
stop_mediators(){
  $home"/bin/stop_all.sh"
}
status(){
  echo "status"
}
start_mediator(){
  echo "starting mediator "$1
  mediators/$1/start.sh
}
stop_mediator(){
  echo "stopping mediator "$1
  mediators/$1/stop.sh
}
do_repair(){
  MEDIATORFOLDER=$home"/mediators"
  FOLDERS=`ls $MEDIATORFOLDER`

  for f in $FOLDERS; do
    cfgfile=$MEDIATORFOLDER"/"$f"/"$f".config"
    cfgbakfile=$MEDIATORFOLDER"/"$f"/"$f".config.bak"
    lckfile=$MEDIATORFOLDER"/"$f"/"$f".lck"
    cfg_size=$(wc -c <"$cfgfile")
    if [ $cfg_size -eq 0 ]; then
      echo $f" config is corrupted"
      if [ -e $lckfile ]; then
        echo "mediator is locked"
      else
        cp $cfgbakfile $cfgfile
        echo "restored from backup"
      fi
    else
      echo $f" config seems to be correct"
    fi
  done

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

  checkout)
    checkout
    ;;
  cpsrc)
    copy_from_source
    ;;
  cpxml)
    copy_xml
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
  startmed)
    start_mediator $2
    ;;
  stopmed)
    stop_mediator $2
    ;;
  status)
    status
    ;;
  repair)
    do_repair
    ;;
  *)

  echo "Commands:======================"
  echo "checkout        get latest code from git repo"
  echo "cpsrc           copy binary files from source"
  echo "cpxml           copy xml files from source"
  echo "build           build sources"
  echo "make            build sources and install bins and ressources"
  echo "start           start server"
  echo "stop            stop server"
  echo "stopall         stop all mediators, then the server"
  echo "startmed [med]  start mediator with name [med]"
  echo "stopmed [med]   stop mediator with name [med]"
  echo "status          print status"
  echo "repair          repair config files if corrupted"

esac
