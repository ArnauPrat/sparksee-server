package edu.upc.dama.sparksee;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

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
						
						resolveQuery(System.currentTimeMillis(), "GRAPH::TYPES", new HashMap<>());

					} catch (Exception e) {
						e.printStackTrace();
					}
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String resolveQuery(Long txId, String code, Map<String, Object> params) throws Exception {
			String queryId = "";
			Map<String, Object> result = new HashMap<String, Object>();

			result.put("gremlin", "g.compute(txId,alg,params)");
		
			DefaultHttpClient httpclient = new DefaultHttpClient();
			try {
				HttpPost httppost = new HttpPost("http://localhost:8182");

				Map<String, Object> bindings = new HashMap<String, Object>();

				bindings.put("txId", txId);

				bindings.put("alg", code);
				bindings.put("params", params);
				result.put("bindings", bindings);
				
				ObjectMapper mapper = new ObjectMapper();
				String content = mapper.writeValueAsString(result);

				content = URLEncoder.encode(content, "UTF-8");

				StringEntity entity = new StringEntity(content);

				httppost.setEntity(entity);

				final HttpResponse response = httpclient.execute(httppost);

				final String json = EntityUtils.toString(response.getEntity());

				final JsonNode node = mapper.readTree(json);
				if (node.get("result").get("data").size() > 0) {
					String ident = node.get("result").get("data").get(0).asText();
					if (!ident.startsWith("QueryException")) {
						final JsonNode nodeId = mapper.readTree(ident);
						nodeId.get("id").toString();
						queryId = nodeId.get("id").toString();

					} else {
						System.out.println("Error solving the Sparksee algebra expression: " + ident);
						throw new Exception("Error");
					}
				}
			} finally {
				httpclient.getConnectionManager().shutdown();
			}

			return queryId;
		}

	}
}
