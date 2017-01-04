package edu.upc.dama.sparksee.commands;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.upc.dama.sparksee.SparkseeServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "Sends the contents of a local script to Sparksee.")
public class UseCommand implements Command {

	@Parameter(names = "--help", help = true)
	private boolean help;

	private JCommander jc;

	@Parameter(names = "-host", required = false, description = "host")
	private String host = "localhost";

	@Parameter(names = "-port", required = false, description = "port")
	private int port = SparkseeServer.getInstance().getPort();

	private String name = "use";

	@Parameter(names = "-file", required = true,
    description = "the database file to use")
	private String file;

	public UseCommand(JCommander jc) {
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
				bindings.put("file", file);

				SparkseeClient.request(host, port, "g.use(file)", bindings);
			} else {
				System.out.println("ERROR: The file does not exist");
			}
		}

	}

	@Override
	public String getName() {

		return name;
	}

}
