package edu.upc.dama.sparksee;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import spark.utils.IOUtils;

public class Client {

	// http://localhost:4567/hello

	public static void main(String[] args) throws Exception {
		Worker[] workers = new Worker[10];

		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker();
		}
		for (Worker w : workers) {
			Thread t = new Thread(w);

			t.setDaemon(false);
			t.start();
		}
		while (true) {
		}
	}

	public static class Worker implements Runnable {

		@Override
		public void run() {
			try {
				URL url = new URL("http://localhost:4567/hello");

				int i = 0;
				while (true) {
					System.out.println("Thread " + Thread.currentThread().getId() + " exec " + i);
					try {
						URLConnection con = url.openConnection();
						con.connect();
						InputStream is = con.getInputStream();
						String s = IOUtils.toString(is);
						
						System.out.println(s);
						is.close();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
