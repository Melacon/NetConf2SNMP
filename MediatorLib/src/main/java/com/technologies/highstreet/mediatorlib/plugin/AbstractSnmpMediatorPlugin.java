package com.technologies.highstreet.mediatorlib.plugin;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.SnmpNetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;

public abstract class AbstractSnmpMediatorPlugin implements ISnmpMediatorPlugin{

    private final SnmpNetworkElement networkElement;
    private final MediatorConfig config;
    public AbstractSnmpMediatorPlugin(SnmpNetworkElement ne,MediatorConfig cfg)
    {
        this.networkElement = ne;
        this.config=cfg;
    }
    /**
     * @return the networkElement
     */
    public SnmpNetworkElement getNetworkElement() {
        return networkElement;
    }
    /**
     * @return the config
     */
    public MediatorConfig getConfig() {
        return config;
    }

}
