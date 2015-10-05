package com.tinkerpop.gremlin.sparksee.structure;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.util.config.YamlConfiguration;

import org.apache.commons.configuration.PropertiesConfiguration;

public class SparkseeGraphTest {
	
	@Test
	public void test(){
		
		
		File f = new File("src/test/resources/sparksee-empty.properties");
		
		Configuration conf = getConfiguration(f);
		
		SparkseeGraph g = SparkseeGraph.open(conf);
		
		long timestamp = System.currentTimeMillis();
		g.begin(timestamp);
		Long transactionId = 1L; 
		g.compute(transactionId, "GRAPH::STATISTICS", new HashMap<String, Object>());
		g.next(1L, 2L);
		g.closeQuery(1L);
		g.commit(transactionId, timestamp);
		
		timestamp = System.currentTimeMillis();
		g.begin(timestamp);
		transactionId = 2L;
		g.compute(transactionId, "GRAPH::STATISTICS", new HashMap<String, Object>());
		g.next(1L, 2L);
		g.commit(transactionId, timestamp);
		
		g.close();
		
		File sparkseeLogDel = new File("sparksee.log");
		sparkseeLogDel.delete();
		
		File testSparkseeDel = new File("test.sparksee");
		testSparkseeDel.delete();
		
		File testSparkseeLogDel = new File("test.sparksee.log");
		testSparkseeLogDel.delete();
		
	}
	
	
	private static Configuration getConfiguration(final File configurationFile) {
        if (null == configurationFile)
            throw Graph.Exceptions.argumentCanNotBeNull("configurationFile");

        if (!configurationFile.isFile())
            throw new IllegalArgumentException(String.format("The location configuration must resolve to a file and [%s] does not", configurationFile));

        try {
            final String fileName = configurationFile.getName();
            final String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            switch (fileExtension) {
                case "yml":
                case "yaml":
                    return new YamlConfiguration(configurationFile);
                default:
                    return new PropertiesConfiguration(configurationFile);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Could not load configuration at: %s", configurationFile), e);
        }
    }

}
