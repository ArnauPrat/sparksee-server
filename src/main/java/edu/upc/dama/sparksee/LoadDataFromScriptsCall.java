package edu.upc.dama.sparksee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sparsity.sparksee.script.ScriptParser;

public class LoadDataFromScriptsCall implements Callable<Boolean> {

	private RemoteGraph graph;

	private File script;

	private String locale;

	private Map<String,String> variables = null;

	private static final Logger LOG = LoggerFactory.getLogger(LoadDataFromScriptsCall.class);

	public LoadDataFromScriptsCall(RemoteGraph graph, File script, String locale, Map<String,String> variables) {
		LOG.debug("requested script to be executed: " + script);
		this.graph = graph;
		this.script = script;
		this.locale = locale;
		this.variables = variables;
	}

	public LoadDataFromScriptsCall(RemoteGraph graph, String content, String locale, Map<String,String> variables) throws IOException {
		script = File.createTempFile("sparksee", ".ddl");
		FileWriter fw = new FileWriter(script);
		try {
			fw.write("create dbgraph mygraph into '"+graph.getDbFile().getCanonicalPath()+"'\n");
			fw.write(content);
		} finally {
			fw.close();
		}
		this.graph = graph;
		this.locale = locale;
		this.variables = variables;
	}

	@Override
	public Boolean call() {

		if (script.exists()) {
			LOG.debug("The file " + script.getAbsolutePath() + " exists");
			while (graph.tx().arePendingTransactions()) {
				LOG.debug("The script " + script.getAbsolutePath() + " needs to wait for active transactions");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					LOG.error("Error runing the script ", e);
					return true;
				}
			}
			;
			LOG.debug("Closing the graph");
			graph.close();
			LOG.debug("Graph closed");
			ScriptParser parser = new ScriptParser();
			for( Map.Entry<String,String> variable : this.variables.entrySet()) {
				//Add variable to script parser
				parser.setVariable(variable.getKey(), variable.getValue());
			}
			if (locale == null) {
				locale = ".utf8";
			}
			try {
				LOG.debug("The script " + script.getAbsolutePath() + " will be executed");
				parser.parse(script.getAbsolutePath(), true, locale);
				LOG.debug("The script " + script.getAbsolutePath() + " has been executed");

			} catch (Exception e) {
				LOG.error("Error runing the script " + script.getAbsolutePath(), e);

			} finally {
				try {
					graph.restart();
				} catch (Exception e) {
					LOG.error("Error runing the script " + script.getAbsolutePath(), e);
				}
			}
		} else {
			LOG.debug("The file " + script.getAbsolutePath() + " does not exist");
		}
		return true;
	}

}
