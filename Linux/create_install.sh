#!/bin/bash

TMPFOLDER=$HOME"/tmp"
REPOFOLDER=$HOME"/odl/Netconf2SNMP/Netconf2SNMP"

copy_files_to_tmp(){

    cp -r $REPOFOLDER/Linux/opt/snmp/bin $TMPFOLDER/
    cp -r $REPOFOLDER/Linux/opt/snmp/yang $TMPFOLDER/
    cp -r $REPOFOLDER/Linux/opt/snmp/nemodel $TMPFOLDER/
    cp $REPOFOLDER/Netconf2SNMPMediator/build/Netconf2SNMPMediator.jar $TMPFolder/bin/
    cp $REPOFOLDER/MediatorServer/bin/MediatorServer.jar $TMPFOLDER/bin/
}

create_pkg_from_tmp(){
 cd $TMPFOLDER
 tar -cvf data.tar *
}

copy_files_to_tmp
create_pkg_from_tmp
