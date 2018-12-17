# Mediatorserver in DockerContainer

This script (control.sh) is for creating a docker container from repository files.
It's tested with Docker 1.5.1 on Ubuntu 16.04.

## Parameters to set

```
MAX_DEVICES=100
API_HOST_IPADDRESS=192.168.178.104
API_HOST_PORT=7070
NETCONF_PORTRANGE_START=4000
SNMP_PORTRANGE_START=10000
JMX_PORTRANGE_START=6000

#it is recommend to add the user to docker group, otherwise you need sudo to run docker commands
DOCKER_NEEDS_SUDO=0
DOCKER_IMAGE_NAME="mediatorserver"
#since docker v1.5 you can use expose for port opening
#for lower version you have to user docker create -p
DOCKER_USE_EXPOSE=1 
```

## Usage

```
control.sh [command]
```

|command|description|
|-------|-----------|
| create | collect all data from the repository into the temporary source folder and build the docker container |
| collect | just collect all data from repository to temporary source folder |
| reconfig | rewrite dockerfile and mediatorserver.conf |
| build | build the docker container from current temporary source folder |
| start [id] | start the mediatorserver container |
| stop [id] | stop the mediatorserver container |
| clean | clean the temporary source folder for the container |
| info | show infos about running mediatorserver container |
| list | show all docker images |
| save [output] | save current image to filename <output> (output will be tar format) |

## Delivery

```
./delivery.sh <version>
```

This will create a <version>.tar.gz and a <version>.tar.gz.sha256 for delivery to give an opportunity to extract, configure and then create a Docker-Image. So from customers site you'll have to

* check the downloaded archive

```
sha256sum --check <version>.tar.gz
```

* extract it into a folder

```
tar -xzf <version.tar.gz>
```

* edit the parameters in control.sh for your purpose (see upwards: Parameters)
* reconfigure

```
./control.sh reconfig
```

* and build it
```
./control.sh build
```

If you want to transfer it to another machine you can afterwards build an archive with
```
./control.sh save
```

But be careful. It generates an tar.gz file which you cannot easily re-import with 
```
docker load
```
