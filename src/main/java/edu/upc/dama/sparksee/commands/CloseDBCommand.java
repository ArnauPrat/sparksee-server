package edu.upc.dama.sparksee.commands;

import java.io.IOException;
import java.util.HashMap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Closes the database. Only use this command when you need to report an error")
public class CloseDBCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	@Parameter(names = "-host", required = false, description = "host")
	private String host = "localhost";

	private String name = "close";

	@Parameter(names = "-port", required = false, description = "server port")
	private int port = 8182;

	public CloseDBCommand(JCommander jc) {
		jc.addCommand(name, this);
		this.jc = jc;
	}

	public void execute() throws IOException {
		if (help) {
			jc.usage(getName());
		} else {

			SparkseeClient.request(host, port, "g.close()", new HashMap<String, Object>());

		}

	}

	@Override
	public String getName() {
		return name;
	}

}
