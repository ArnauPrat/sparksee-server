package edu.upc.dama.sparksee.commands;

import static spark.Spark.stop;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import edu.upc.dama.sparksee.RemoteGraph;
import edu.upc.dama.sparksee.SparkseeServer;

import java.util.HashMap;

@Parameters(commandDescription = "Shutdown the server that has started in that machine")
public class ShutdownCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

    @Parameter(names = "-host", required = false, description = "host")
    private String host = "localhost";

    @Parameter(names = "-port", required = false, description = "port")
    private int port = SparkseeServer.getInstance().getPort();

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
            SparkseeClient.request(host, port, "g.shutdown()", new HashMap<String, Object>());
		}
	}

	@Override
	public String getName() {		
		return name;
	}

}
