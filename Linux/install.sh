#!/bin/bash

#
# write config file for server
# to /etc/mediatorserver.conf
#
write_config()
{
home=$1
user=$2
fn="/etc/mediatorserver.conf"
echo "#global config file for mediatorserver" > $fn
echo "\n" >> $fn
echo "#Home Directory" >> $fn
echo "home="$home >> $fn
echo "\n" >> $fn
echo "#HOST IP" >> $fn
echo "host=192.168.11.11" >> $fn
echo "port=7070" >> $fn
echo "\n" >> $fn
echo "#Port range for Netconf" >> $fn
echo "ncrangemin=4000" >> $fn
echo "ncrangemax=6000" >> $fn
echo "\n" >> $fn
echo "#Port Range for SNMP" >> $fn
echo "snmprangemin=10000" >> $fn
echo "snmprangemax=12000" >> $fn
echo "\n" >> $fn
echo "#Log" >> $fn
echo "logfile=/var/log/mediatorserver.log" >> $fn

}
write_systemdconfig()
{
home=$1
user=$2
fn="/etc/systemd/system/mediator.service"

echo "[Unit]" > $fn
echo "Description=mediatorserver" >>$fn
echo "After=network.target" >> $fn
echo "\n" >> $fn
echo "[Service]" >< $fn
echo "WorkingDirectory=$home" >> $fn
echo "SyslogIdentifier=MediatorServer" >> $fn
echo "ExecStart="$home"bin/mediatorserver.sh" >> $fn
echo "User="$user >> $fn
echo "Type=oneshot" >> $fn
echo "\n" >> $fn
echo "[Install]" >> $fn
echo "WantedBy=multi-user.target" >> $fn

}

create_folders()
{
  home=$1
  user=$2
#create home

  mkdir $home;
  mkdir $home"bin"
  mkdir $home"mediators"
  mkdir $home"log"
  mkdir $home"nemodel"
  mkdir $home"yang"
  mkdir $home"source"

  chown -R $user":"$user $home

}

# main part

echo "******************************************"
echo "*            MediatorServer              *"
echo "*----------------------------------------*"
echo "*      Highstreet Technologies 2017      *"
echo "*                                        *"
echo "******************************************"

inp_user="mediators"
inp_home="/opt/snmp/"
inp_zipfile="install_data.zip"

#create user
echo "\ncreating user..."
useradd $inp_user
#get home
#create folders
echo -n "choose a home directory[$inp_hom]:"
$inp=read()
if [ -n "$inp" ]; then 
  inp_home=$inp
fi
echo "\nwriting configuration"
write_config $inp_home $inp_user
echo "\ncreating folders..."
create_folders $inp_home $inp_user
#get dev or prod option
echo "choose your installation:\n[1] developer\n[2] production";
echo -n "select[1]:"
dev=read()
if [ -z $dev ] then;
  dev=1
fi;

#install prerequisites
echo "\ninstall required packages..."
apt-get update
apt-get -y install jq curl unzip
if [ $dev == 1 ] then;
  apt-get -y install openjdk-8-jdk maven 
else
  apt-get -y install openjdk-8-jre
fi;

#unzipping files to bin, yang and nemodel

#creating systemd daemon
write_systemdconfig $inp_home $inp_user
systemctl daemon-reload

#creating cronjob for firewall


#if dev
#checkout repository
echo "\ngetting source code..."
git clone https://git-highstreet-technologies.com/highstreet/Netconf2SNMP.git


echo "\ninstallation completed!"
