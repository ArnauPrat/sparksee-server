package edu.upc.dama.sparksee.commands;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import edu.upc.dama.sparksee.SparkseeServer;

@Parameters(commandDescription = "Sends the contents of a local script to Sparksee.")
public class ScriptCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	@Parameter(names = "-host", required = false, description = "host")
	private String host = "localhost";

	@Parameter(names = "-port", required = false, description = "port")
	private int port = SparkseeServer.getInstance().getPort();

	private String name = "run";

	@Parameter(names = "-file", required = true, 
			description = "file with the script contents. Remove the \"create/use dbgraph\" line from your script")
	private String file;

	public ScriptCommand(JCommander jc) {
		jc.addCommand(name, this);
		this.jc = jc;
	}

	@Override
	public void execute() throws Exception {
		if (help) {
			jc.usage(getName());
		} else {

			File script = new File(file);

			if (script.exists()) {
				String contents = FileUtils.readFileToString(script);
				HashMap<String, Object> bindings = new HashMap<String, Object>();
				bindings.put("scriptContent", contents);
				bindings.put("locale", ".utf8");

				SparkseeClient.request(host, port, "g.script(scriptContent, locale)", bindings);
			} else {
				System.out.println("ERROR: The script does not exist");
			}

		}

	}

	@Override
	public String getName() {

		return name;
	}

}
