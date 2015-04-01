package com.tinkerpop.gremlin.sparksee.structure;

import java.util.Map;

public interface SparkseeGraphMBean {

	public String compute(String algebra,Map<String, Object> params);
	
	public String next(Long queryId, Long rows);
	
	public String closeQuery(Long queryId);
	
	public String getLicense();
    
    public String getDatabaseFile();
}
