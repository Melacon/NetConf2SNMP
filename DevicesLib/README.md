### Description

This is a Library Project for the Netconf2SNMPMediator. It keeps device specific data and classes for snmp devices.


### Build


```
mvn clean package
```


### HowTo add a new DeviceType

These are the instructions to create a new device type for the Netconf2SNMPMediator project. An Example is given as ExampleSNMPDevice.

* extend the enum SNMPDeviceType.java
* create a class for the DeviceAlerts in data
* create a class for the device itself in devices (extending BaseSNMPDevice)
* extend the CREATOR in data.BaseSNMPDevice.java
* extend the method getAvailableAlerts in data.BaseSNMPDevice.java