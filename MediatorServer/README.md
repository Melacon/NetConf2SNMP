### Description

Webserver to Provide an API to control and create the Server on which multiple instances of the Netconf2SNMPMediator runs to control everything via HTTP.

### Usage

```
java -classpath MediatorServer.jar com.technologies.highstreet.mediatorserver.server.WebAppServer [<config-filename>]

```
Default config file has to be placed in /etc/mediatorserver.conf

### Config File

```
#Home Directory
home=/opt/snmp/

#HOST IP
host=192.168.11.11
port=7070

#Port range for Netconf
ncrangemin=4000
ncrangemax=6000

#Port Range for SNMP
snmprangemin=10000
snmprangemax=12000

#PortRange for JMX
jmxrangemin=6001
jmxrangemax=7000

#Log (ERROR | WARN | DEBUG | INFO | TRACE )
loglevel=WARN
logfile=/var/log/mediatorserver.log

#=====================================
#global MediatorConfig

#set LogLevel (ERROR | WARN | DEBUG | INFO | TRACE )
MediatorLogLevel=DEBUG

#set ping timeout in milliseconds
MediatorDevicePingTimeout=2000

#set latency for snmp requests
MediatorSnmpLatency=2000

#set java memory for mediator instance
MediatorMemory="-Xmx256m -Xms128m"

```
* HostIP... is the IPAddress of the mediator server which is used to register the mediators on the OpenDaylight-Server
* port  ... is the HTTP-Port for the API to get access to the API-functions

### HTTP-API

```cli
http://<host>:<port>/api/?task=<task>
```


| Task         | additional Parameters | Description | Response (Success) |
| --- | --- | --- | --- |
| create       | config=&lt;json-config-file&gt;  | create new mediator instance | {"code":1,"data":"&lt;string&gt;"} |
| delete       | name=&lt;name&gt; | delete mediator instance | \{"code":1,"data":"&lt;string&gt;"} |
| start        | name=&lt;name&gt; | start mediator instance | \{"code":1,"data":"&lt;string&gt;"} |
| stop         | name=&lt;name&gt; | stop mediator instance | \{"code":1,"data":"&lt;string&gt;"} |
| getconfig    | name=&lt;name&gt;(optional) | Get current Config for all instances / named mediator instance | \{"code":1,"data":[]}|
| getlog       | name=&lt;name&gt;(optional) | Get LogEntries for all instances / named mediator instance | \{"code":1,"data":[]} |
| clearlock    | name=&lt;name&gt; | Clear Mediator Lock File | \{"code":1,"data":"&lt;string&gt;"} |
| getnemodels  | - | get all Network Element Template Filenames | \{"code":1,"data":[&lt;string-array&gt;]} |
| getncports   | limit=&lt;limit&gt;(optional) | get next free ports for Netconf Connections | \{"code":1,"data":[&lt;int-array&gt;]} |
| getsnmpports | limit=&lt;limit&gt;(optional) | get next free ports for SNMP Traps | \{"code":1,"data":[&lt;int-array&gt;]} |


HTTP-Response is always a json-formatted String with 2 Elements:

* code ... 1:success 0:failure
* data ... if code==0: &lt;string&gt; else &lt;string | object&gt;

### Javascript Class

Here is a small documentation about how to use the JS class MediatorServer which is located in [here](www/js/class.mediator.js).

```Javascript
var serverURL = "http://192.168.178.89:7070"
var mediatorServer = new MediatorServer(serverURL);

mediatorServer.LoadConfigs(function(configs){
    if(configs!==undefined && configs.length>0)
    {
        for(var i=0;i<configs.length;i++)
            ...
    }
});
```
Up to now this lib uses jquery to do the POST-Requests. But this can easily be changed. Therefore you just have to change the internal method "post".

Now you can use the public methods of the MediatorServer object:

| Method | Description |
| --- | --- |
| StartMediator(name,callback) |  |
| StopMediator(name,callback) | |
| CreateMediator(name,devicetype,deviceip,trapsPort,nexml,ncport, callback) | 
| DeleteMediator(name,callback) | |
| ClearLock(name,callback) | |
| LoadNetworkElementXMLFiles(callback) | |
| LoadConfigs(callback) | |


MediatorDeviceTypes:

| Value | enumValue | Name |
| --- | --- | --- |
| 0 | DEVICETYPE_SIMULATOR | "Simulator" |
| 1 | DEVICETYPE_EXAMPLE_DEVICE | "Example Device" |


MediatorConfig class:

| Property | Type | Description |
| --- | ---- | --- |
| Name | String | Name of the mediator |
| DeviceType | int | see table MediatorDeviceTypes |
| DeviceIp | String | remote IP of the snmp device |
| TrapsPort | int | snmp port of the mediator |
| NetconfPort | int | netconf port of the mediator |
| NeModel | String | XML-Template filename the mediator uses |
| IsNetConfConnected | boolean | flag if the mediator is connected to ODL |
| PID | int | the linux process-id (0 if not running, >0 if is currently running |
| IsLocked | boolean | flag that lock file exists |
| Autorun  | boolean | flag that run file exists |
| FirewallRuleActive | boolean | flag that snmp port NAT rule was created |
| OpenDaylightConfigs | Array[&lt;ODLData&gt;] | Opendaylight access data to send login and logout |


### TODO

* read mediators logs
* implement getperformance task
