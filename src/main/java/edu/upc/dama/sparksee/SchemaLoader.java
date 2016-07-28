package edu.upc.dama.loader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class SchemaLoader {

	public static void main(String[] args) {
		
		String host = args[0]; //myhost
		String port = args[1]; //8182
		String fileName = args[2]; //src/main/resources/db-media-schema.ddl
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("gremlin", "g.script(script,locale)");

		@SuppressWarnings("resource")
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httppost = new HttpPost("http://myhost:8182");

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> bindings = new HashMap<String, Object>();
			File file = new File(fileName);
			FileUtils.readFileToString(file);
			bindings.put("script", FileUtils.readFileToString(file));
			bindings.put("locale", "");
			result.put("bindings", bindings);

			String content = mapper.writeValueAsString(result);

			StringEntity entity = new StringEntity(content);

			httppost.setEntity(entity);
			final HttpResponse response = httpclient.execute(httppost);
			final String json = EntityUtils.toString(response.getEntity());
			System.out.println("json Sparksee response: "+json);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
