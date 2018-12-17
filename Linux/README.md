### Description

This represents the Filesystem on a Ubuntu Server 16.04 with all files and scripts to run a complete mediator server including:

* multiple Mediator-Instances
* the HTTP-API-Server

### Prerequisites

The description bases on the environment: Ubuntu 16.04, Java 1.8, git 2.7.4, curl

### Installation instructions

* create user "mediators"
```
adduser mediators
```

* define a folder as $NETCONF2SNMP_HOME

```
echo "NETCONF2SNMP_HOME=/opt/snmp/" >> /etc/environment
```

* checkout repo folder to this folder

* add MediatorServer.jar to systemd
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



