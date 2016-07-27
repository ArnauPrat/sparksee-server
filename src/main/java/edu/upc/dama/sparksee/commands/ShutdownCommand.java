package edu.upc.dama.sparksee.commands;

import static spark.Spark.stop;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import edu.upc.dama.sparksee.RemoteGraph;
import edu.upc.dama.sparksee.SparkseeServer;

@Parameters(commandDescription = "Shutdown the server that has started in that machine")
public class ShutdownCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	private String name = "shutdown";

	public ShutdownCommand(JCommander jc) {
		jc.addCommand(name, this);
		this.jc = jc;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jc.usage(getName());
		} else {
			RemoteGraph graph = SparkseeServer.getInstance().getGraph();
			if(graph != null){
				graph.close();
				stop();
			}
		}
	}

	@Override
	public String getName() {		
		return name;
	}

}
