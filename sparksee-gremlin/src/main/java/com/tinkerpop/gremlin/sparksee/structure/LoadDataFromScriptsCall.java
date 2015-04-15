package com.tinkerpop.gremlin.sparksee.structure;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sparsity.sparksee.script.ScriptParser;

public class LoadDataFromScriptsCall implements Callable<Boolean> {

	private SparkseeGraph graph;

	private String script;

	private String locale;
	
	private static final Logger LOG = LoggerFactory
			.getLogger(LoadDataFromScriptsCall.class);

	public LoadDataFromScriptsCall(SparkseeGraph graph, String script,
			String locale) {
		LOG.debug("requested script to be executed: "+script);
		this.graph = graph;
		this.script = script;
		this.locale = locale;
	}

	@Override
	public Boolean call()  {
		
		File file = new File(script);
		if (file.exists()) {
			LOG.debug("The file "+file.getAbsolutePath()+" exists");
			while (graph.tx().arePendingTransactions()) {
				LOG.debug("The script "+file.getAbsolutePath()+" needs to wait for active transactions");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					LOG.error("Error runing the script ",e);
					return true;
				}
			};
			LOG.debug("Closing the graph");
			graph.close();
			LOG.debug("Graph closed");
			ScriptParser parser = new ScriptParser();
			if (locale == null) {
				locale = ".utf8";
			}
			try {
				LOG.debug("The script "+file.getAbsolutePath()+" will be executed");
				parser.parse(file.getAbsolutePath(), true, locale);
				LOG.debug("The script "+file.getAbsolutePath()+" has been executed");

			} catch (Exception e) {
				LOG.error("Error runing the script "+file.getAbsolutePath(),e);				
				
			} finally {
				try {
					graph.restart();
				} catch (Exception e) {
					LOG.error("Error runing the script "+file.getAbsolutePath(),e);
				}
			}
		}
		else{
			LOG.debug("The file "+file.getAbsolutePath()+" does not exist");
		}
		return true;
	}

}
