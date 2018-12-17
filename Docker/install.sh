#!/bin/bash

# ======================================================= #
#                                                         #
#              Generic MediatorServer                     #
#               Installation Script                       #
#               for Docker delivery                       #
#                                                         #
# ------------------------------------------------------- #

# Parameters Section ======================================


TARFILENAME="mediatorserver*.tar"
MEDIATORS_JSON="mediators.json"
. install.conf


# Vars Section ================================================
NETCONF_PORTRANGE_END=$((NETCONF_PORTRANGE_START+MAX_DEVICES))
SNMP_PORTRANGE_END=$((SNMP_PORTRANGE_START+MAX_DEVICES))
JMX_PORTRANGE_END=$((JMX_PORTRANGE_START+MAX_DEVICES))
DOCKER_IMAGE_NAME="mediatorserver"
DOCKER_IMAGE_ID=""
DNSOPTION=""

if [ ! -z "$DNS" ]; then
  DNSOPTION="--dns="$DNS
fi

STARTSCRIPT_ENVVARS="-e API_HOST_IPADDRESS="$API_HOST_IPADDRESS" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e API_HOST_PORT="$API_HOST_PORT" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e NETCONF_PORTRANGE_START="$NETCONF_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e SNMP_PORTRANGE_START="$SNMP_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e JMX_PORTRANGE_START="$JMX_PORTRANGE_START" "
STARTSCRIPT_ENVVARS=$STARTSCRIPT_ENVVARS"-e MAX_DEVICES="$MAX_DEVICES" "

# Function Section ========================================

do_install(){
  echo -n "importing docker image from archive..."
  $SUDO docker load -i $TARFILENAME
  echo "done"
}
do_start(){
  echo -n "starting docker image..."
  PORTS="-p "$API_HOST_PORT":"$API_HOST_PORT
  PORTS=$PORTS" -p 162:162/udp"
  PORTS=$PORTS" -p "$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END":"$NETCONF_PORTRANGE_START"-"$NETCONF_PORTRANGE_END
  PORTS=$PORTS" -p "$SNMP_PORTRANGE_START"-"$SNMP_PORTRANGE_END":"$SNMP_PORTRANGE_START"-"$SNMP_PORTRANGE_END
  PORTS=$PORTS" -p "$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START":"$JMX_PORTRANGE_START"-"$JMX_PORTRANGE_START

  $SUDO docker run $DNSOPTION --cap-add=NET_ADMIN --cap-add=NET_RAW $PORTS $STARTSCRIPT_ENVVARS -d $DOCKER_IMAGE_NAME
  echo "done"
}
do_stop(){
  echo -n "stopping docker image..."
  $SUDO docker stop $DOCKER_IMAGE_ID
  echo "done"
}
do_status(){
 $SUDO docker inspect $DOCKER_IMAGE_ID
}
do_create_mediators(){
  CURL=`which curl`
  if [ -z "$CURL" ]; then
    echo "cannot install mediators on server. curl is missing"
    return
  fi
  echo "installing mediators on server..."
  while IFS=$' \t\n\r' read -r LINE
  do
    JSON=$JSON${LINE// /}
  done < $MEDIATORS_JSON
  URL="http://"$API_HOST_IPADDRESS":"$API_HOST_PORT"/api/?task=create&config="$JSON

  echo $URL
  curl -g $URL
  echo "done"
}
print_help(){

  echo "commands:"
  echo "  install         load docker image from package"
  echo "  start           run docker image with given parameters in background"
  echo "  stop            stop docker image"
  echo "  status          show status of currently running docker image"
  echo "  mediators       install mediators from mediators.json"
  echo "  help            show help"

}

# Script Body ============================================
DOCKER_IMAGE_ID=$($SUDO docker ps -q --filter ancestor=$DOCKER_IMAGE_NAME)


case "$1" in

  install)
    do_install
    ;;
  start)
    do_start
    ;;
  stop)
    do_stop
    ;;
  status)
    do_status
    ;;
  mediators)
    do_create_mediators
    ;;
  *)
    print_help
    ;;

esac
