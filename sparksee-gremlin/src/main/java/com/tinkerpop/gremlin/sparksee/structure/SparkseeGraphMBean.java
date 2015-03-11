package com.tinkerpop.gremlin.sparksee.structure;

public interface SparkseeGraphMBean {

	public String compute(String algebra);
	
	public String next(Long queryId, Long rows);
	
	public String closeQuery(Long queryId);
	
	public String getLicense();
    
    public String getDatabaseFile();
}
