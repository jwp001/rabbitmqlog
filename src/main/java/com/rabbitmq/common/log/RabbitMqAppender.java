package com.rabbitmq.common.log;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.rabbitmq.common.log.utils.RabbitMQUtils;

/**
 * Created by jinwenpeng on 2017/4/27.
 */
@Plugin(
		name = "RabbitMQ",
		category = Node.CATEGORY,
		elementType = Appender.ELEMENT_TYPE,
		printObject = true)
public final class RabbitMqAppender extends AbstractAppender {

	private final Connection[] connections;
	private String queue;
	private String routeKey;
	private String exchange;
	private RabbitMQUtils rabbitMQUtils = new RabbitMQUtils();


	/**
	 *
	 * @param name
	 * @param filter
	 * @param layout
	 * @param ignoreExceptions
	 * @param connections
	 * @param queue
	 * @param routeKey
	 * @param exchange
	 */
	private RabbitMqAppender(
			final String name,
			final Filter filter,
			final Layout<? extends Serializable> layout,
			final boolean ignoreExceptions,
			final Connection[] connections,
			final String queue,
			final String routeKey,
			final String exchange) {
		super(name, filter, layout, ignoreExceptions);
		this.connections = connections;
		this.queue = queue;
		this.routeKey = routeKey;
		this.exchange = exchange;
	}

	@Override
	public void start() {
		if(connections != null && connections.length > 0) {
			for(Connection connection : connections) {
				try{
					Property[] properties = connection.getProperties();
					String userName = "";
					String password = "";
					String hostName = "";
					int port = 0;
					String virtualHost = "";
					if(properties != null && properties.length > 0) {
						for(Property property : properties) {
							if(RabbitMqAppenderConstants.UESERNAME.equalsIgnoreCase(property.getName())) {
								userName = property.getValue();
							} else if(RabbitMqAppenderConstants.PASSWORD.equalsIgnoreCase(property.getName())) {
								password = property.getValue();
							}else if(RabbitMqAppenderConstants.HOSTNAME.equalsIgnoreCase(property.getName())) {
								hostName = property.getValue();
							}else if(RabbitMqAppenderConstants.PORT.equalsIgnoreCase(property.getName())) {
								port = Integer.valueOf(property.getValue());
							}else if(RabbitMqAppenderConstants.VIRTUALHOST.equalsIgnoreCase(property.getName())) {
								virtualHost = property.getValue();
							}
						}
						rabbitMQUtils.addConnection(userName,password,virtualHost,hostName,port);
					}
				}catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}

			}
		}
		super.start();
	}

	public void append(LogEvent logEvent) {
		final byte[] bytes = getLayout().toByteArray(logEvent);// 日志二进制文件，输出到指定位置就行
		// 下面这个是要实现的自定义逻辑
		try {
			rabbitMQUtils.publish(exchange,routeKey,queue,null,bytes);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}

	@PluginFactory
	public static RabbitMqAppender createAppender(
			@PluginAttribute("name")
			final String name,
			@PluginAttribute("ignoreExceptions")
			final boolean ignoreExceptions,
			@PluginAttribute("exchange")
			final String exchange,
			@PluginAttribute("routeKey")
			final String routeKey,
			@PluginAttribute("queue")
			final String queue,
			@PluginElement("Filter")
			final Filter filter,
			@PluginElement("Layout")
			Layout<? extends Serializable> layout,
			@PluginElement("Connections")
			final Connection[] connections) {
		if (name == null) {
			LOGGER.error("No name provided for MyCustomAppenderImpl");
			return null;
		}
		if (queue == null) {
			LOGGER.error("No queue provided for MyCustomAppenderImpl");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new RabbitMqAppender(name, filter, layout, ignoreExceptions,
				connections,queue,routeKey,exchange);
	}

}
