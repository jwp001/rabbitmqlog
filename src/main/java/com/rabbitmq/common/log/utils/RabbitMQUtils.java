package com.rabbitmq.common.log.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;


import org.apache.commons.lang.StringUtils;

import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * RabbitMQ工具类
 */

public class RabbitMQUtils {

	private Logger log = LogManager.getLogger(getClass());

	/**
	 * 添加连接
	 * 
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @param virtualHost
	 *            虚拟主机
	 * @param hostName
	 *            主机名
	 * @param portNumber
	 *            主机端口号
	 */
	public RabbitMQUtils addConnection(
			String userName,
			String password,
			String virtualHost,
			String hostName,
			int portNumber) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(userName);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(portNumber);
		resourceSet.add(factory);
		return this;
	}

	/**
	 * 添加连接，形如：amqp://userName:password@hostName:portNumber/virtualHost
	 * 
	 * @param uri
	 *            连接的URI
	 * @throws URISyntaxException
	 *             If the given string violates RFC&nbsp;2396, as augmented by
	 *             the above deviations
	 */
	public RabbitMQUtils addConnection(String uri) throws URISyntaxException,
			KeyManagementException, NoSuchAlgorithmException {
		ConnectionFactory factory = new ConnectionFactory();
		this.setUri(factory, new URI(uri));
		resourceSet.add(factory);
		return this;
	}

	/**
	 * Convenience method for setting the fields in an AMQP URI: host, port,
	 * username, password and virtual host. If any part of the URI is ommited,
	 * the ConnectionFactory's corresponding variable is left unchanged.
	 * 
	 * @param uri
	 *            is the AMQP URI containing the data
	 */
	private void setUri(ConnectionFactory factory, URI uri)
			throws URISyntaxException, NoSuchAlgorithmException,
			KeyManagementException {
		if ("amqp".equals(uri.getScheme().toLowerCase())) {
			// nothing special to do
		} else if ("amqps".equals(uri.getScheme().toLowerCase())) {
			factory.setPort(ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT);
			factory.useSslProtocol();
		} else {
			throw new IllegalArgumentException("Wrong scheme in AMQP URI: " +
					uri.getScheme());
		}

		String host = uri.getHost();
		if (host != null) {
			factory.setHost(host);
		}

		int port = uri.getPort();
		if (port != -1) {
			factory.setPort(port);
		}

		String userInfo = uri.getRawUserInfo();
		if (userInfo != null) {
			String userPass[] = userInfo.split(":");
			if (userPass.length > 2) {
				throw new IllegalArgumentException("Bad user info in AMQP " +
						"URI: " + userInfo);
			}

			factory.setUsername(uriDecode(userPass[0]));
			if (userPass.length == 2) {
				factory.setPassword(uriDecode(userPass[1]));
			}
		}

		String path = uri.getRawPath();
		if (path != null && path.length() > 0) {
			factory.setVirtualHost(uriDecode(uri.getPath()));
		}
	}

	private String uriDecode(String s) {
		try {
			// URLDecode decodes '+' to a space, as for
			// form encoding. So protect plus signs.
			return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取所有的连接工场
	 * 
	 * @return Set
	 */
	public Set<ConnectionFactory> getConnectionFactorys() {
		return resourceSet;
	}

	/**
	 * 发送消息
	 * 
	 * @param exchangeName
	 *            exchange Name。如果为空，则routingKey必须为空；而queueName则必须有值
	 * @param routingKey
	 *            路由key。如果为空，则exchangeName必须为空；而queueName则必须有值
	 * @param queueName
	 *            对队名。如果为空，则exchangeName和routingKey则必须有值
	 * @param basicProperties
	 *            配置
	 * @param message
	 *            消息体
	 * @throws IOException
	 *             连接异常
	 * @throws TimeoutException
	 *             连接超时
	 */
	public void publish(
			String exchangeName,
			String routingKey,
			String queueName,
			AMQP.BasicProperties basicProperties,
			byte[] message)
			throws IOException, TimeoutException, ParamIsNullException {

		Connection conn = getConnection();// 获取连接
		Channel channel = getChannel(conn, exchangeName, routingKey, queueName);

		try {
			if (!exchangeName.isEmpty() && !routingKey.isEmpty()) {// 有exchangeName和routingKey的情况
				channel.basicPublish(exchangeName, routingKey,
						basicProperties,
						message);
			} else if (StringUtils.isNotEmpty(queueName)) {// 只有queue的情况
				channel.basicPublish("", queueName, basicProperties,
						message);
			}
		} finally {
			if (channel != null)
				channel.close();
			conn.close();
		}
	}

	/**
	 * 获取channel
	 * 
	 * @param conn
	 *            连接
	 * @param exchangeName
	 *            exchange Name
	 * @param routingKey
	 *            routing Key
	 * @param queueName
	 *            queue Name
	 * @return Channel
	 * @throws ParamIsNullException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	private Channel getChannel(
			Connection conn,
			String exchangeName,
			String routingKey,
			String queueName)
			throws ParamIsNullException, IOException, TimeoutException {
		check(exchangeName, routingKey, queueName);// 校验入参

		Channel channel = null;
		channel = conn.createChannel();
		if (!exchangeName.isEmpty() && !routingKey.isEmpty()) {// 有exchangeName和routingKey的情况
			channel.exchangeDeclare(exchangeName, "direct", true);

			// 如果没有传queue，通过channel进行获取
			if (StringUtils.isEmpty(queueName)) {
				queueName = channel.queueDeclare().getQueue();
			} else {
				channel.queueDeclare(queueName, true, false, false,
						null);
			}
			channel.queueBind(queueName, exchangeName, routingKey);
		} else if (StringUtils.isNotEmpty(queueName)) {// 只有queue的情况
			channel.queueDeclare(queueName, true, false, false, null);
		}
		return channel;
	}

	/**
	 * 发送校验入参
	 *
	 * @param exchangeName
	 *            exchange Name。如果为空，则routingKey必须为空；而queueName则必须有值
	 * @param routingKey
	 *            路由key。如果为空，则exchangeName必须为空；而queueName则必须有值
	 * @param queueName
	 *            对队名。如果为空，则exchangeName和routingKey则必须有值
	 * @throws ParamIsNullException
	 *             当校验出现问题
	 */
	private void check(String exchangeName, String routingKey, String queueName)
			throws ParamIsNullException {
		if (StringUtils.isEmpty(exchangeName)
				&& StringUtils.isEmpty(routingKey)
				&& StringUtils.isEmpty(queueName)) {
			throw new ParamIsNullException(
					"exchangeName、routingKey和queueName不能同时为空");
		}
		if ((StringUtils.isNotEmpty(exchangeName)
				&& StringUtils.isEmpty(routingKey))
				|| (StringUtils.isEmpty(exchangeName)
						&& StringUtils.isNotEmpty(routingKey))) {
			throw new ParamIsNullException(
					"exchangeName和routingKey必须同时有值");
		}
	}

	/**
	 * 循环连接工场，连上哪个算哪个
	 * 
	 * @return Connection
	 */
	private Connection getConnection() throws ConnectException {
		Connection conn = null;
		for (ConnectionFactory connectionFactory : this.resourceSet) {
			try {
				conn = connectionFactory.newConnection();
				if (conn.isOpen()) {
					break;
				}
			} catch (IOException | TimeoutException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (conn == null || !conn.isOpen())
			throw new ConnectException("所有mq都无法连接！");
		return conn;
	}

	/**
	 * 接收
	 * 
	 * @param exchangeName
	 *            exchange Name
	 * @param routingKey
	 *            routing Key
	 * @param queueName
	 *            queue Name
	 * @param handler
	 *            RabbitMQMessageHandler 收到消息的回调函数
	 * @throws Exception Exception
	 */
	public void receive(
			String exchangeName,
			String routingKey,
			String queueName,
			RabbitMQMessageHandler handler) throws Exception {

		Connection connection = this.getConnection();
		Channel channel = null;
		try {
			channel = getChannel(connection, exchangeName, routingKey,
					queueName);
			// channel.basicQos(1);// 实现公平调度的方式就是让每个消费者在同一时刻会分配一个任务。
			// boolean durable = true;

			QueueingConsumer consumer = new QueueingConsumer(channel);

			// 取消 autoAck
			boolean autoAck = false;
			channel.basicConsume(queueName, autoAck, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery = null;
				try {
					delivery = consumer.nextDelivery();
					log.trace(new String(delivery.getBody()));
					handler.setMessage(delivery.getBody());
				} catch (ConsumerCancelledException e) {// 重连
					Thread.sleep(500);// 半秒
					channel = getChannel(connection, exchangeName, routingKey,
							queueName);
					consumer = new QueueingConsumer(channel);

					// 取消 autoAck
					channel.basicConsume(queueName, autoAck, consumer);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				} finally {
					try {
						// 确认消息，已经收到
						channel.basicAck(delivery.getEnvelope()
								.getDeliveryTag(), false);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		} finally {
			if (channel != null)
				channel.close();
			if (connection != null)
				connection.close();
		}
	}

	private final HashSet<ConnectionFactory> resourceSet = new HashSet<>();

}
