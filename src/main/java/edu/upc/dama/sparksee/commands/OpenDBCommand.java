package edu.upc.dama.sparksee.commands;

import java.io.IOException;
import java.util.HashMap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import edu.upc.dama.sparksee.SparkseeServer;

@Parameters(commandDescription = "Open the closed database.")
public class OpenDBCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	@Parameter(names = "-host", required = false, description = "host")
	private String host = "localhost";

	@Parameter(names = "-port", required = false, description = "port")
	private int port = SparkseeServer.getInstance().getPort();

	private String name = "open";

	public OpenDBCommand(JCommander jc) {
		jc.addCommand(name, this);
		this.jc = jc;
	}

	public void execute() throws IOException {
		if (help) {
			jc.usage(getName());
		} else {
			SparkseeClient.request(host, port, "g.restart()", new HashMap<String, Object>());
		}
	}

	@Override
	public String getName() {
		return name;
	}
}
