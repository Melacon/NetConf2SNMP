package com.technologies.highstreet.netconf2snmpmediator.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.technologies.highstreet.mediatorlib.netconf.server.basetypes.SnmpKeyValuePairList;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NetworkElement;
import com.technologies.highstreet.mediatorlib.netconf.server.networkelement.NodeEditConfigCollection;
import com.technologies.highstreet.mediatorlib.netconf.server.types.MediatorConfig;
import com.technologies.highstreet.mediatorlib.netconf.server.types.NetconfTagList;
import com.technologies.highstreet.mediatorlib.plugin.AbstractMediatorPlugin;
import com.technologies.highstreet.mediatorlib.plugin.AbstractSnmpMediatorPlugin;

public class PluginableMediator {
    private final List<AbstractMediatorPlugin> plugins;
    private final List<AbstractSnmpMediatorPlugin> snmpPlugins;

    private final File pluginPath;

    private static Log LOG = LogFactory.getLog(PluginableMediator.class);

    public PluginableMediator() {
        this("plugins/");
    }

    public PluginableMediator(String plgPath) {
        this.plugins = new ArrayList<>();
        this.snmpPlugins = new ArrayList<>();
        File f = new File(plgPath);
        if (f.exists() && f.isDirectory()) {
            this.pluginPath = f;
        } else {
            this.pluginPath = null;
        }
    }

    protected void _loadPlugins(NetworkElement ne, MediatorConfig cfg) {
        if (pluginPath == null) {
            return;
        }
        if (pluginPath.exists() && pluginPath.isDirectory()) {
            URLClassLoader child;
            LOG.debug("loading plugins");
            for (File jarFile : pluginPath.listFiles()) {
                String fn = jarFile.getName();
                String nsName = fn.substring(0, fn.length() - ".jar".length());
                String clsName = nsName + ".Plugin";
                if (fn.endsWith(".jar")) {
                    LOG.debug("try to load " + jarFile.getName());
                    URL[] urls = null;
                    Class<?>[] cArg = new Class[] { NetworkElement.class };
                    try {
                        urls = new URL[] { jarFile.toURI().toURL() };
                    } catch (MalformedURLException e) {
                        LOG.warn("urlerror:" + e.getMessage());
                    }
                    if (urls != null) {
                        child = new URLClassLoader(urls);
                        Class<?> classToLoad;
                        Object obj = null;
                        try {
                            classToLoad = Class.forName(clsName, false, child);
                            obj = classToLoad.getConstructor(cArg).newInstance(ne, cfg);
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                                | SecurityException e) {
                            LOG.warn("failed to load jar:" + e.getMessage());
                        }
                        if (obj != null) {
                            if (obj instanceof AbstractMediatorPlugin) {
                                this.plugins.add((AbstractMediatorPlugin) obj);
                            } else if (obj instanceof AbstractSnmpMediatorPlugin) {
                                this.snmpPlugins.add((AbstractSnmpMediatorPlugin) obj);
                            }
                        }
                        try {
                            child.close();
                        } catch (IOException e) {
                            LOG.warn("problem loading plugin:" + e.getMessage());
                        }
                    }

                }
            }
            LOG.debug("finished loading plugins");

        }
    }

    public void addPlugin(AbstractMediatorPlugin p) {
        this.plugins.add(p);
    }

    public void addPlugin(AbstractSnmpMediatorPlugin p) {
        this.snmpPlugins.add(p);
    }

    protected void _plgPreInit() {
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPreInit();
            } catch (Exception e) {
                LOG.warn("problem executing onPreInit for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPreInit();
            } catch (Exception e) {
                LOG.warn("problem executing onPreInit for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }

    protected void _plgPostInit() {
        LOG.debug("handle postInit");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPostInit();
            } catch (Exception e) {
                LOG.warn("problem executing onPostInit for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPostInit();
            } catch (Exception e) {
                LOG.warn("problem executing onPostInit for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }
    protected void _plgPreRequest(String messageId, NetconfTagList tags)
    {
        this._plgPreRequest(messageId, tags,null);
    }
    protected void _plgPreRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {
        LOG.debug("handle preRequest");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPreRequest(messageId,tags);
            } catch (Exception e) {
                LOG.warn("problem executing onPreRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPreRequest(messageId,tags,nodes);
            } catch (Exception e) {
                LOG.warn("problem executing onPreRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }
    protected void _plgPostRequest(String messageId, NetconfTagList tags)
    {
        this._plgPostRequest(messageId, tags, null);
    }
    protected void _plgPostRequest(String messageId, NetconfTagList tags, NodeEditConfigCollection nodes) {
        LOG.debug("handle postRequest");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPostRequest(messageId,tags);
            } catch (Exception e) {
                LOG.warn("problem executing onPostRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPostRequest(messageId,tags,nodes);
            } catch (Exception e) {
                LOG.warn("problem executing onPostRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }

    protected void _plgPreEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
        LOG.debug("handle preEditRequest");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPreEditRequest(messageId,tags,sourceMessage);
            } catch (Exception e) {
                LOG.warn("problem executing onPreEditRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPreEditRequest(messageId,tags,sourceMessage);
            } catch (Exception e) {
                LOG.warn("problem executing onPreEditRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }

    protected void _plgPostEditRequest(String messageId, NetconfTagList tags, Document sourceMessage) {
        LOG.debug("handle postEditRequest");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onPostEditRequest(messageId,tags,sourceMessage);
            } catch (Exception e) {
                LOG.warn("problem executing onPostEditRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onPostEditRequest(messageId,tags,sourceMessage);
            } catch (Exception e) {
                LOG.warn("problem executing onPostEditRequest for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }
    protected void _plgClose() {
        LOG.debug("handle Close");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onClose();
            } catch (Exception e) {
                LOG.warn("problem executing onClose for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onClose();
            } catch (Exception e) {
                LOG.warn("problem executing onClose for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }


    protected boolean _plgOnTrapReceived(SnmpKeyValuePairList trap) {
        boolean handled = false;
        LOG.debug("handle trapReceived 2");
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                if (plugin.onTrapReceived(trap)) {
                    handled = true;
                }
            } catch (Exception e) {
                LOG.warn("problem executing onTrapReceived for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage()+ org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));

            }
        }
        return handled;

    }

    protected void _plgOnDeviceConnectionStatusChanged(int before,int now)
    {
        LOG.debug("handle onDeviceConnectionChanged");
        for (AbstractMediatorPlugin plugin : this.plugins) {
            try {
                plugin.onDeviceConnectionStatusChanged(before, now);
            } catch (Exception e) {
                LOG.warn("problem executing onDeviceConnectionChanged for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
        for (AbstractSnmpMediatorPlugin plugin : this.snmpPlugins) {
            try {
                plugin.onDeviceConnectionStatusChanged(before, now);
            } catch (Exception e) {
                LOG.warn("problem executing onDeviceConnectionChanged for plugin " + plugin.getClass().getSimpleName() + " :"
                        + e.getMessage());
            }
        }
    }

}
