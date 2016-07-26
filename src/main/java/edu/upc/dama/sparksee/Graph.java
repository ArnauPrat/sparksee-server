package edu.upc.dama.sparksee;

import java.util.Map;

public class Graph {

	public String compute(long txId, String alg, Map<String, Object> params){
		return "{ \"id\" : 1 }";
	}
	
	public String compute(String alg, Map<String, Object> params){
		return "{ \"id\" : 1 }";
	}
	
	public String getWS(long txId){
		return "";
	}
	
	public String garbageCollect(long timestamp){
		return "";
	}
	
	public String commit(long txId, long timestamp){
		return "";
	}
	
	public String rollback(long txId){
		return "";
	}
	
	public String redoWS(long transactionId, long commitTimestamp, long precommitId){
		return "";
	}
	
	public String begin(long timestamp){
		return "{ \"id\" : 1 }";
	}
	
	public String closeQuery(long queryid){
		return "{}";
	}
	
	public String next(long queryid, int rows){
		return "{\\\"columns\\\":[{\\\"ID\\\":\\\"LONG\\\"},{\\\"ID_1\\\":\\\"LONG\\\"}],\\\"rows\\\":[[8194,3074],[8195,3075]]}";
	}
}
