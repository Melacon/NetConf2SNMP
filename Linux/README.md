### Description

This represents the Filesystem on a Ubuntu Server 16.04 with all files and scripts to run a complete mediator server including:

* multiple Mediator-Instances
* the HTTP-API-Server

### Prerequisites

The description bases on the environment: Ubuntu 16.04, Java 1.8, git 2.7.4, curl , jq

### Installation instructions

* create user "mediators"
```
adduser mediators
```

* define a folder as home (this will be your working directory)

```
e.g. /opt/snmp/
```

* create a folder source
```
mkdir /opt/snmp/source
```
* checkout repo folder to this folder
```
cd /opt/snmp/source
git clone <repo>.git
```
* copy the files and folders that the structure now looks like:
```
/opt/snmp/bin
/opt/snmp/nemodel
/opt/snmp/source
/opt/snmp/yang
```

* add MediatorServer instance to systemd
* create new file: /etc/systemd/system/mediatorserver.service

```
[Unit]
Description=mediatorserver
After=network.target

[Service]
WorkingDirectory=/opt/snmp/bin/
SyslogIdentifier=MediatorServer
ExecStart=/opt/snmp/bin/mediatorserver.sh
User=mediators
Type=oneshot

[Install]
WantedBy=multi-user.target

```

* afterwards run following commands
```
systemctl daemon-reload
```
```
systemctl enable mediatorserver.service
```

* create crons
```
crontab -e
```
```
*/2 * * * * /bin/bash /opt/snmp/bin/firewall.sh >> /var/log/firewall.log
*/2 * * * * /bin/bash /opt/snmp/bin/clean_all.sh > /dev/null 2>&1

```

* start the server
```
./bin/control.sh start
```