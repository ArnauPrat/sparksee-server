package edu.upc.dama.sparksee;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import edu.upc.dama.sparksee.commands.*;

public class SparkseeServer {
	@Parameter(names = "--help", help = true)
	private boolean help;

	private int port = 8182;

	private static SparkseeServer server = null;
	
	private RemoteGraph graph;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private SparkseeServer() {
	}

	public static SparkseeServer getInstance() {
		if (server == null) {
			server = new SparkseeServer();
		}
		return server;
	}
	
	public RemoteGraph getGraph(){
		return graph;
	}
	
	public void setGraph(RemoteGraph graph){
		this.graph = graph;
	}

	public static void main(String[] args) throws Exception {

		Map<String, Command> commands = new HashMap<String, Command>();
		SparkseeServer server = getInstance();
		JCommander jc = new JCommander(server);

		StartCommand start = new StartCommand(jc);
		OpenDBCommand open = new OpenDBCommand(jc);
		CloseDBCommand close = new CloseDBCommand(jc);
		ScriptCommand script = new ScriptCommand(jc);
		ShutdownCommand shutdown = new ShutdownCommand(jc);
		UseCommand use = new UseCommand(jc);
		
		commands.put(start.getName(), start);
		commands.put(open.getName(), open);
		commands.put(close.getName(), close);
		commands.put(script.getName(), script);
		commands.put(shutdown.getName(), shutdown);
		commands.put(use.getName(),use);

		jc.parse(args);
		String command = jc.getParsedCommand();
		if (server.help || args.length == 0) {
			jc.usage();
		} else if (commands.containsKey(command)) {
			Command selectedCommand = commands.get(command);
			selectedCommand.execute();
		}
	}
}
