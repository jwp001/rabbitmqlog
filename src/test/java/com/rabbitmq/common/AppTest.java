package com.rabbitmq.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Unit test for simple App.
 */
public class AppTest {
	public static Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException {

		Runnable r = new Runnable() {
			@Override
			public void run() {

				for (int i = 0; i < 100; i++)
					log.info("Hello World!");
			}
		};
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(100);
		// new Thread(r).start();
		// Thread.sleep(5000);
		// new Thread(r).start();
		for (int i = 0; i < 100; i++)
			new Thread(r).start();

	}
}
