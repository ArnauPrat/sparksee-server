package edu.upc.dama.sparksee;

import java.util.Properties;

import eu.coherentpaas.wrapper.Graph;

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

				int i = 0;
				while (true) {
					System.out.println("Thread " + Thread.currentThread().getId() + " exec " + i);
					try {
						Properties properties = new Properties();
						properties.put("sparksee.host", "localhost");
						properties.put("sparksee.port", "8182");
						Graph graph = new Graph(properties);
						graph.begin();
						graph.compute("GRAPH::TYPES");
						graph.applyWS();

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
