package com.technologies.highstreet.mediatorlib.plugin;

import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;

public abstract class AbstractMediatorPlugin implements IMediatorPlugin {

	protected final NetworkElement networkElement;
	protected final MediatorConfig config;
	public AbstractMediatorPlugin(NetworkElement ne,MediatorConfig cfg)
	{
		this.networkElement = ne;
		this.config=cfg;
	}


}
