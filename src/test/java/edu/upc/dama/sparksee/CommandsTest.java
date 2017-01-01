package edu.upc.dama.sparksee;

import org.junit.Test;

public class CommandsTest {

	@Test
	public void testShutdown() throws Exception{
		SparkseeServer.main(new String[]{"start"});
		Thread.sleep(1000);
		SparkseeServer.main(new String[]{"shutdown"});
		Thread.sleep(7000);
	}
	
	@Test
	public void testClose() throws Exception{
		SparkseeServer.main(new String[]{"start"});
		
		Thread.sleep(1000);
		
		SparkseeServer.main(new String[]{"close"});
		
		SparkseeServer.main(new String[]{"shutdown"});
		Thread.sleep(7000);
	}
	
	@Test
	public void testScript() throws Exception{
		SparkseeServer.main(new String[]{"start"});
		
		Thread.sleep(1000);
		
		SparkseeServer.main(new String[]{"run", "-file", "src/test/resources/schema.ddl"});
		Thread.sleep(5000);
		
		SparkseeServer.main(new String[]{"shutdown"});
		Thread.sleep(7000);
	}
}
