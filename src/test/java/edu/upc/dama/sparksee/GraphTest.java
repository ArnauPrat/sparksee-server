package edu.upc.dama.sparksee;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.junit.Assert;
import org.junit.Test;


public class GraphTest {

	@Test
	public void testOpen() throws Exception{
		Configurations configs = new Configurations();
		Configuration config = configs.properties(new File("src/test/resources/database.properties"));
		RemoteGraph graph = RemoteGraph.open(config);
		Assert.assertTrue(true);
	}
}
