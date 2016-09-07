package edu.upc.dama.sparksee.commands;

import static spark.Spark.port;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import edu.upc.dama.sparksee.RemoteGraph;
import edu.upc.dama.sparksee.SparkseeServer;
import spark.Request;
import spark.Response;
import spark.Route;

@Parameters(commandDescription = "Starts the server")
public class StartCommand implements Command {

	@DynamicParameter(names = "-D", description = "Dynamic configuration parameters go here")
	private Map<String, String> params = new HashMap<>();

	@Parameter(names = "-db", required = false, description = "database file")
	private String database = "graph.gdb";

	@Parameter(names = "-port", required = false, description = "port")
	private int port = SparkseeServer.getInstance().getPort();

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	private String name = "start";

	public StartCommand(JCommander jc) {
		jc.addCommand(name, this);
		this.jc = jc;
	}

	public void execute() throws IOException {
		if (help) {
			jc.usage(getName());
		} else {

			if (params == null || params.isEmpty()) {
				params = new HashMap<String, String>();
				params.put("sparksee.license", "V8CW9-XCDE6-A7YGA-K43PC");
				params.put("sparksee.io.recovery", "true");
			}
			RemoteGraph graph = RemoteGraph.open(params,
					new File("dbs" + File.separator + database).getCanonicalFile());
			SparkseeServer.getInstance().setPort(port);
			SparkseeServer.getInstance().setGraph(graph);
			port(port);
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
			
			
			post("/", new Route() {

				@Override
				public Object handle(Request request, Response response) throws Exception {
					ObjectMapper mapper = new ObjectMapper();
					String entity = URLDecoder.decode(request.body(), "UTF-8");
					JsonNode data = mapper.readTree(entity);
					String code = data.get("gremlin").asText();

					String json = "";
					Object result = null;
					
					
					
					Throwable error = null;
					try {
						//start = System.currentTimeMillis();
						@SuppressWarnings("unchecked")
						Map<String, ?> params = mapper.convertValue(data.get("bindings"), Map.class);
						
						Bindings binding = new SimpleBindings();
						binding.putAll(params);
						binding.put("g", graph);
						
						
						result =engine.eval(code, binding);
					
					} catch (Throwable e) {
						e.printStackTrace();
						error = e;
					} 
					if (error == null) {
						json = mapper.writeValueAsString(new GraphResponse(result));
					} else {
						json = mapper.writeValueAsString(new GraphResponse("QueryException: " + error.getMessage()));
					}

					return json;
				}

			});
			
			System.out.println("Sparksee server started. Ready to accept connections on port: "+port);
		}
	}

	public class GraphResponse {

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

	@Override
	public String getName() {

		return name;
	}
}
