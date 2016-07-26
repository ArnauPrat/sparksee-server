package edu.upc.dama.sparksee;

import static spark.Spark.port;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import spark.Request;
import spark.Response;
import spark.Route;

public class SparkseeServer {
	public static void main(String[] args) throws ConfigurationException, IOException {
		
		Configurations configs = new Configurations();
		Configuration config = configs.properties(new File("src/test/resources/database.properties"));
		RemoteGraph graph = RemoteGraph.open(config);
		port(8182);
		post("/", new Route() {

			@Override
			public Object handle(Request request, Response response) throws Exception {
				ObjectMapper mapper = new ObjectMapper();
				String entity = URLDecoder.decode(request.body(), "UTF-8");
				JsonNode data = mapper.readTree(entity);
				String code = data.get("gremlin").asText();

				Map<?, ?> params = mapper.convertValue(data.get("bindings"), Map.class);

				Binding binding = new Binding(params);
				binding.setVariable("g", graph);
				GroovyShell shell = new GroovyShell(binding);
				Object result = null;
				Throwable error = null;
				try {
					result = shell.evaluate(code);
				} catch (Throwable e) {
					error = e;
				}

				String json = "";
				if (error == null) {
					json = mapper.writeValueAsString(new GraphResponse(result));
				} else {
					json = mapper.writeValueAsString(
							new GraphResponse(
							"QueryException: " + error.getMessage()));
				}
				return json;
			}

		});
	}

	public static class GraphResponse {

		private Result result;

		private Map<String, Object> meta = new HashMap<String, Object>();

		private String requestId;

		private Status status;

		public GraphResponse(Object... data) {
			result = new Result(data);
			status = new Status();
			requestId = Long.toString(System.currentTimeMillis());
		}

		public Result getResult() {
			return result;
		}

		public Map<String, Object> getMeta() {
			return meta;
		}

		public String getRequestId() {
			return requestId;
		}

		public Status getStatus() {
			return status;
		}

		public class Result {
			private Object[] data;

			public Result(Object... data) {
				this.data = data;
			}

			public Object[] getData() {
				return data;
			}
		}

		public class Status {

			private String code = "200";

			private Map<String, Object> attributes = new HashMap<String, Object>();

			private String message = "";

			public Status() {
			}

			public Status(String code, Map<String, Object> attributes, String message) {
				this.code = code;
				this.attributes = attributes;
				this.message = message;
			}

			public String getCode() {
				return code;
			}

			public Map<String, Object> getAttributes() {
				return attributes;
			}

			public String getMessage() {
				return message;
			}

		}
	}
}
