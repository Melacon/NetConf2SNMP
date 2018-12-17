#!/bin/bash

# ------------------------------------------------------------
# ------------ Script Definitions

# ---------------- Parameters 
. install.conf
#MAX_DEVICES=100
#API_HOST_IPADDRESS=172.17.0.2
#API_HOST_IPADDRESS=192.168.178.104
#API_HOST_PORT=7070
#NETCONF_PORTRANGE_START=4000
#SNMP_PORTRANGE_START=10000
#JMX_PORTRANGE_START=6000

NETCONF_PORTRANGE_END=$((NETCONF_PORTRANGE_START+MAX_DEVICES-1))
SNMP_PORTRANGE_END=$((SNMP_PORTRANGE_START+MAX_DEVICES-1))
JMX_PORTRANGE_END=$((JMX_PORTRANGE_START+MAX_DEVICES-1))
ALLPORTS=($API_HOST_PORT $NETCONF_PORTRANGE_START $NETCONF_PORTRANGE_END $SNMP_PORTRANGE_START $SNMP_PORTRANGE_END $JMX_PORTRANGE_START $JMX_PORTRANGE_END)
MINPORT=${ALLPORTS[0]}
MAXPORT=${ALLPORTS[0]}
for i in "${ALLPORTS[@]}"; do
  (( i > MAXPORT )) && MAXPORT=$i
  (( i < MINPORT )) && MAXPORT=$i
done

DNSOPTION=""
if [ ! -z "$DNS" ]; then
  DNSOPTION="--dns="$DNS
fi

#it is recommend to add the user to docker group, otherwise you need sudo to run docker commands
DOCKER_NEEDS_SUDO=0
DOCKER_IMAGE_NAME="mediatorserver"
#since docker v1.5 you can use expose for port opening
#for lower version you have to user docker create -p
DOCKER_USE_EXPOSE=1
ISDEV=1

# ---------------- Vars
# ---------------- pls do not change
DOCKER_IMAGE_ID=""
DOCKERFILE_DEV_TMPL=Dockerfile.dev.tmpl
DOCKERFILE_PROD_TMPL=Dockerfile.prod.tmpl

DOCKERFILE_TMPL=$DOCKERFILE_PROD_TMPL

DOCKERFILE=Dockerfile
DOCKER_SRC_FOLDER="src/"
REPO_FOLDER="../"
REPO_FOLDER_BASEMEDIATOR=$REPO_FOLDER"Netconf2SNMPMediator/"
REPO_FOLDER_MEDIATORSERVER=$REPO_FOLDER"MediatorServer/"
REPO_FOLDER_FILESYSTEM=$REPO_FOLDER"Linux/"

STARTSCRIPT_ENVVARS="-e API_HOST_IPADDRESS="$API_HOST_IPADDRESS" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e API_HOST_PORT="$API_HOST_PORT" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e NETCONF_PORTRANGE_START="$NETCONF_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e SNMP_PORTRANGE_START="$SNMP_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e JMX_PORTRANGE_START="$JMX_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e MAX_DEVICES="$MAX_DEVICES" "

# ------------------------------------------------------------
# ------------ Script Functions

copy_from_sources(){
  echo -n "creating file structure in docker source dir..."
  mkdir -p $DOCKER_SRC_FOLDER"config"
  mkdir -p $DOCKER_SRC_FOLDER"opt/snmp"
  mkdir $DOCKER_SRC_FOLDER"opt/snmp/mediators"
  mkdir $DOCKER_SRC_FOLDER"opt/snmp/nemodel"
  mkdir $DOCKER_SRC_FOLDER"opt/snmp/bin"
  echo "done"
  echo -n "copying latest files from repository..."
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/clean_all.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/control.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/debug.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/firewall.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/mediatorserver.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/Netconf2SNMPMediator.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/snmpserialbridge.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/snmpxmlcreator.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/start_all.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_FILESYSTEM"opt/snmp/bin/stop_all.sh" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp -r $REPO_FOLDER_BASEMEDIATOR"yang" $DOCKER_SRC_FOLDER"opt/snmp/"
  #get jars
  cp $REPO_FOLDER_MEDIATOR"build/Netconf2SNMPMediator.jar" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  cp $REPO_FOLDER_MEDIATORSERVER"bin/MediatorServer.jar" $DOCKER_SRC_FOLDER"opt/snmp/bin/"
  #get nexmls
  cp $REPO_FOLDER_BASEMEDIATOR"xmlNeModel/DVM_MWCore12_BasicAir.xml" $DOCKER_SRC_FOLDER"opt/snmp/nemodel/"
  #get the rest
  cp crontab $DOCKER_SRC_FOLDER"/"
  cp start.sh $DOCKER_SRC_FOLDER"/"
  cp $REPO_FOLDER_FILESYSTEM"etc/mediatorserver.tmpl.conf" $DOCKER_SRC_FOLDER"/"
  echo "done"
}
write_mediatorserver_config(){
  DSTFILE=$1
  TMPLFILE=$REPO_FOLDER_FILESYSTEM"etc/mediatorserver.tmpl.conf"
  echo -n "writing mediatorserver config to $DSTFILE..."
  c=`cat $TMPLFILE`
  c="${c/APIHOST/$API_HOST_IPADDRESS}"
  c="${c/APIPORT/$API_HOST_PORT}"
  c="${c/NCRANGEMIN/$NETCONF_PORTRANGE_START}"
  c="${c/NCRANGEMAX/$NETCONF_PORTRANGE_END}"
  c="${c/SNMPRANGEMIN/$SNMP_PORTRANGE_START}"
  c="${c/SNMPRANGEMAX/$SNMP_PORTRANGE_END}"
  c="${c/JMXRANGEMIN/$JMX_PORTRANGE_START}"
  c="${c/JMXRANGEMAX/$JMX_PORTRANGE_END}"
  echo "$c" > $DSTFILE
  echo "done"
}
write_dockerfile(){
  echo -n "writing docker file..."
  cat $DOCKERFILE_TMPL > $DOCKERFILE
  if [ $DOCKER_USE_EXPOSE -eq 1 ]; then
     if [ $ISDEV -eq 1 ]; then
        echo "EXPOSE $API_HOST_PORT" >> $DOCKERFILE
        echo "EXPOSE $NETCONF_PORTRANGE_START-$NETCONF_PORTRANGE_END" >> $DOCKERFILE
        echo "EXPOSE $SNMP_PORTRANGE_START-$SNMP_PORTRANGE_END" >> $DOCKERFILE
        echo "EXPOSE $JMX_PORTRANGE_START-$JMX_PORTRANGE_END" >> $DOCKERFILE
     else
        echo "EXPOSE $MINPORT-$MAXPORT" >> $DOCKERFILE
     fi
  fi
  echo "done"
}
do_clean(){
  echo -n "cleaning docker src dir..."
  rm -rf $DOCKER_SRC_FOLDER
  echo "done"
}
do_reconfig(){
  if [ ! -z "$1" ];then
    MEDIATORSERVERCONFIGFILE=$1
  else
    MEDIATORSERVERCONFIGFILE=$DOCKER_SRC_FOLDER"mediatorserver.tmpl.conf"
  fi
  if [ -f "$MEDIATORSERVERCONFIGFILE" ]; then
    echo "no mediatorserver config template (filename="$MEDIATORSERVERCONFIGFILE")found"
    exit 1
  fi
  write_mediatorserver_config $DOCKER_SRC_FOLDER"config/mediatorserver.conf" $MEDIATORSERVERCONFIGFILE
  write_dockerfile
}
do_build(){
  write_dockerfile
  do_clean
  copy_from_sources
  write_mediatorserver_config $DOCKER_SRC_FOLDER"config/mediatorserver.conf"
  $SUDO docker build -t $DOCKER_IMAGE_NAME .
}
do_create(){
  echo -n "creating container with envvars ("$STARTSCRIPT_ENVVARS")..."
  PORTS="-p "$API_HOST_PORT":"$API_HOST_PORT
  PORTS=$PORTS" -p 162:162/udp"
  PORTS=$PORTS" -p "$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END":"$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END
  PORTS=$PORTS" -p "$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START":"$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START
  $SUDO docker create $DNSOPTION --cap-add=NET_ADMIN --cap-add=NET_RAW $PORTS $STARTSCRIPT_ENVVARS --name $DOCKER_IMAGE_NAME
  echo "done"
}
do_run(){
  if [ ! -z "$1" ]; then
    DOCKER_IMAGE_ID=$1
  fi
  echo -n "starting docker container..."
#  $SUOD docker start -i $DOCKER_IMAGE_ID
  PORTS="-p "$API_HOST_PORT":"$API_HOST_PORT
  PORTS=$PORTS" -p 162:162/udp"
  PORTS=$PORTS" -p "$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END":"$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END
  PORTS=$PORTS" -p "$SNMP_PORTRANGE_START"-"$SNMP_PORTRANGE_END":"$SNMP_PORTRANGE_START"-"$SNMP_PORTRANGE_END
  PORTS=$PORTS" -p "$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START":"$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START
  $SUDO docker run $DNSOPTION --cap-add=NET_ADMIN --cap-add=NET_RAW $PORTS $STARTSCRIPT_ENVVARS -t $DOCKER_IMAGE_NAME
  echo "done"
}
do_stop(){
  if [ ! -z "$1" ]; then
    DOCKER_IMAGE_ID=$1
  fi
  echo -n "try to stop docker container..."
  $SUDO docker stop $DOCKER_IMAGE_ID
  echo "done"
}
#
# Export docker container to tar
# optional parameter #1...filename
#
do_save(){

  if [ ! -z "$1" ]; then
    DSTTARFILENAME=$1
  else
    DSTTARFILENAME="mediatorserver.tar"
  fi
  echo -n "saving docker container..."
  $SUDO docker save -o $DSTTARFILENAME $DOCKER_IMAGE_NAME
  echo "done"
}


# ------------------------------------------------------------
# ------------ Script Body
SUDO="sudo "
if [ $DOCKER_NEEDS_SUDO -eq 0 ]; then
  SUDO=""
fi
spaces="                              "
here=$(pwd)
DOCKER_IMAGE_ID=$($SUDO docker ps -q --filter ancestor=$DOCKER_IMAGE_NAME)

echo "****************************************************"
echo "* Control Script for DockerImage of MediatorServer *"
echo "* ------------------------------------------------ *"
echo "* config:                                          *"
echo "*       ApiServer: http(s)://$API_HOST_IPADDRESS:$API_HOST_PORT  *"
echo "*   Netconf-Range: ${NETCONF_PORTRANGE_START:0:5}${spaces:0:$((5-${#NETCONF_PORTRANGE_START}))} - ${NETCONF_PORTRANGE_END:0:5}${spaces:0:$((5-${#NETCONF_PORTRANGE_END}))}                   *"
echo "*      Snmp-Range: ${SNMP_PORTRANGE_START:0:5}${spaces:0:$((5-${#SNMP_PORTRANGE_START}))} - ${SNMP_PORTRANGE_END:0:5}${spaces:0:$((5-${#SNMP_PORTRANGE_END}))}                   *"
echo "*       JMX-Range: ${JMX_PORTRANGE_START:0:5}${spaces:0:$((5-${#JMX_PORTRANGE_START}))} - ${JMX_PORTRANGE_END:0:5}${spaces:0:$((5-${#JMX_PORTRANGE_END}))}                   *"
echo "*         MinPort: ${MINPORT:0:30}${spaces:0:$((30-${#MINPORT}))}  *"
echo "*         MaxPort: ${MAXPORT:0:30}${spaces:0:$((30-${#MAXPORT}))}  *"
echo "* ------------------------------------------------ *"
echo "* currentImage:                                    *"
echo "*      tag: ${DOCKER_IMAGE_NAME:0:30}${spaces:0:$((30-${#DOCKER_IMAGE_NAME}))}         *"
echo "*       id: ${DOCKER_IMAGE_ID:0:30}${spaces:0:$((30-${#DOCKER_IMAGE_ID}))}         *"
echo "****************************************************"

case "$1" in

  create)
    do_create
    ;;
  build)
    do_build
    ;;
  collect)
    do_clean
    copy_from_sources
    ;;
  reconfig)
    do_reconfig $2
    ;;
  start)
    do_run $2
    ;;
  stop)
    do_stop $2
    ;;
  list)
    $SUDO docker ps -a
    ;;
  clean)
    do_clean
    ;;
  info)
    $SUDO docker inspect $DOCKER_IMAGE_ID
    ;;
  save)
    do_save $2
    ;;
 *)

  echo "Commands:======================"
  echo "create          create docker image from repository"
  echo "collect         just collect all data from repository to temporary source folder"
  echo "build           just build the docker with current temporary source folder"
  echo "reconfig        rewrite dockerfile and mediatorserver.conf"
  echo "start [ID]      start latest created docker image"
  echo "stop [ID]       stop docker image"
  echo "list            list docker images"
  echo "clean           remove all temporary collected sources"
  echo "info            show information for docker image"
  echo "save [output]   save current image to filename <output> | output will be tar.gz format"

esac

