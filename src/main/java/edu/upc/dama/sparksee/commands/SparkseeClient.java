package edu.upc.dama.sparksee.commands;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.HttpClients;

public class SparkseeClient {

	public static HttpResponse request(String host, int port, String command, Map<String, Object> bindings)
			throws IOException {

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("gremlin", command);

		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpPost httppost = new HttpPost("http://" + host + ":" + port);

		result.put("bindings", bindings);
		ObjectMapper mapper = new ObjectMapper();
		String content = mapper.writeValueAsString(result);

		content = URLEncoder.encode(content, "UTF-8");

		StringEntity entity = new StringEntity(content);

		httppost.setEntity(entity);

        return httpclient.execute(httppost);
	}

}
