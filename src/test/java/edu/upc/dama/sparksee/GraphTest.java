package edu.upc.dama.sparksee;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class GraphTest {

	@Test
	public void testOpen() throws Exception {

		File file = new File("graph.gdb").getCanonicalFile();
		File parent = file.getParentFile();
		if (file.exists()) {
			File[] subfiles = parent.listFiles();
			for (File subfile : subfiles) {
				if (subfile.getName().startsWith("graph.gdb")) {
					subfile.delete();
				}
			}
		}

		HashMap<String, String> params = new HashMap<String, String>();
		RemoteGraph graph = RemoteGraph.open(params, new File("graph.gdb").getCanonicalFile());
		try {
			Assert.assertNotNull(graph);
		} finally {
			file = new File("graph.gdb").getCanonicalFile();
			if (file.exists()) {
				File[] subfiles = parent.listFiles();
				for (File subfile : subfiles) {
					if (subfile.getName().startsWith("graph.gdb")) {
						subfile.delete();
					}
				}
			}
		}
	}
}
