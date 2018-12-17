/**
 *
 */
package com.technologies.highstreet.netconf.server.control;

/**
 * @author herbert
 *
 */
public interface NetconfNotifyOriginator {

    //public void setNetconfNotifyExecutor( NetconfNotifyExecutor command );

	public void addNetconfNotifyExecutor(BaseNetconfController netconfProcessor);
	public void removeNetconfNotifyExecutor(BaseNetconfController netconfProcessor);

}
