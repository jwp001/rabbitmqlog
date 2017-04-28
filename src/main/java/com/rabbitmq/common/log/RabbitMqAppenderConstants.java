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

/**
 * Created by jinwenpeng on 2017/4/27.
 */
public interface RabbitMqAppenderConstants  {

	String UESERNAME = "userName";//用户名
	String PASSWORD = "password";//密码
	String HOSTNAME = "hostName";//mq地址
	String PORT = "port";//端口
	String VIRTUALHOST = "virtualHost";//虚拟主机
	String EXCHANGE = "exchange";//消息交换机名称
	String ROUTEKEY = "routeKey";//路由关键字
	String QUEUE = "queue";//消息队列名称
}
