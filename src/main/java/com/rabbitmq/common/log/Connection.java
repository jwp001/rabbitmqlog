package com.rabbitmq.common.log;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * Created by jinwenpeng on 2017/4/27.
 */
@Plugin(name = "Connection", category = Node.CATEGORY, printObject = true)
public class Connection {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private Property[] properties;

    public Property[] getProperties() {
        return properties;
    }

    private Connection(Property[] properties) {
        this.properties = properties;
    }

    @PluginFactory
    public static Connection createConnection(@PluginElement("Properties") Property[] properties) {
        if(properties == null) {
            LOGGER.error("Property name cannot be null");
        }
        return new Connection(properties);
    }
}
