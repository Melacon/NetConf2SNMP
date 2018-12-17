package com.technologies.highstreet.netconf.server.control;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.Console;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.netconf.server.basetypes.MessageStore;

public interface BaseNetconfController extends NetconfNotifyExecutor {

    public abstract <T extends NetworkElement> void start(MessageStore messageStore, T ne, Console console);
    public abstract void destroy();

}
