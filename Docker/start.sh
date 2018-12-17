#!/bin/bash

SERVER_CONFIG_FILE="/etc/mediatorserver.conf"
SERVER_CONFIG_TMPL_FILE="/etc/mediatorserver.tmpl.conf"

create_config(){
  echo -n "writing mediatorserver config to $SERVER_CONFIG_FILE..."
  c=`cat $SERVER_CONFIG_TMPL_FILE`
  c="${c/APIHOST/$API_HOST_IPADDRESS}"
  c="${c/APIPORT/$API_HOST_PORT}"
  c="${c/NCRANGEMIN/$NETCONF_PORTRANGE_START}"
  c="${c/NCRANGEMAX/$NETCONF_PORTRANGE_END}"
  c="${c/SNMPRANGEMIN/$SNMP_PORTRANGE_START}"
  c="${c/SNMPRANGEMAX/$SNMP_PORTRANGE_END}"
  c="${c/JMXRANGEMIN/$JMX_PORTRANGE_START}"
  c="${c/JMXRANGEMAX/$JMX_PORTRANGE_END}"
  echo "$c" > $SERVER_CONFIG_FILE
  echo "done"
}

HOMEFOLDER=/opt/snmp
#starting cronjobs
echo "starting cron daemon"
cron
sshcmd=`which sshd`
if [ ! -z "$sshcmd" ]; then 
  echo "starting ssh server"
  /usr/sbin/sshd
fi

#rewrite mediatorserver.conf
#API_HOST_IPADDRESS, API_HOST_PORT, NETCONF_PORTRANGE_START,NC_PORTRANGE_MAX, SNMP_PORTRANGE_START,JMX_PORTRANGE_START, MAX_DEVICES
if [ ! -f "$SERVER_CONFIG_FILE" ]; then
  NETCONF_PORTRANGE_END=$((NETCONF_PORTRANGE_START+MAX_DEVICES))
  SNMP_PORTRANGE_END=$((SNMP_PORTRANGE_START+MAX_DEVICES))
  JMX_PORTRANGE_END=$((JMX_PORTRANGE_START+MAX_DEVICES))
  create_config
fi

#starting server application
echo "starting mediatorserver"
cd $HOMEFOLDER 
su mediators ./bin/mediatorserver.sh

echo "mediatorserver closed"
