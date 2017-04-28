package com.rabbitmq.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
    public static Logger log = LogManager.getLogger(App.class);
	public static void main(String[] args) throws InterruptedException {
		for(int i = 0; i < 10000; i++) {
			log.info("Hello World!");
			Thread.sleep(2);
			log.error("Hello World!=================================");
			Thread.sleep(2);
		}

	}
}
