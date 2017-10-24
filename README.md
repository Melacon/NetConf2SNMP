### Description

This project is to integrate SNMP devices into the OpenDayLight project. Therefore every SNMP device will be represented with one mediator-instance. On one server there can run multiple instances of this mediator. To control the whole server and the mediator instances there will be an HTTP-API which could be accessed by the ODL GUI.
 
SubProjects:

* [DevicesLib](DevicesLib/): Basic definitions for SNMP devices
* [Netconf2SNMPMediator](Netconf2SNMPMediator/): Mediator for translating SNMP to netconf and back
* [MediatorServer](MediatorServer/): HTTP-API server (Only the java project)
* [Linux](Linux/README.md): Shows all files and scripts on the mediator server
* [mwtnMediator](mwtnMediator/): ux module for OpenDaylight talking to the MediatorServer

### Installation

TODO

### Usage

```
***********************************************************
*                 OpenDaylight GUI                        *
*                                                         *
*                                                         *
***********************************************************

***********************************************************
*               HTTP-API(MediatorServer)                  *
*---------------------------------------------------------*
*                                                         *
*             Ubuntu Server 16.04 LTS (Linux)             *
*                                                         *
*                                                         *
*                                                         *
*   **************     **************     **************  *
*   * Mediator 1 *     * Mediator 2 *     * Mediator 3 *  *
*   **************     **************     **************  *
*                                                         *
*   **************     **************     **************  *
*   * Mediator 4 *     * Mediator 5 *     * Mediator 6 *  *
*   **************     **************     **************  *
*                                                         *
*                                                         *
* --------------------------------------------------------*
*            \      Firewall Port NAT     /               *
*             \       (SNMP Traps)       /                *
***********************************************************


    *****************             *****************
    * SNMP Device 1 *             * SNMP Device 2 *
    *****************             *****************

    *****************             *****************
    * SNMP Device 3 *             * SNMP Device 5 *
    *****************             *****************

    *****************             *****************
    * SNMP Device 5 *             * SNMP Device 6 *
    *****************             *****************


```


### License
