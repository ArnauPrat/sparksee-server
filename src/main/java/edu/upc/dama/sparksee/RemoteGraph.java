package edu.upc.dama.sparksee;

import spark.Spark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteGraph {
	protected static final int INVALID_TYPE = com.sparsity.sparksee.gdb.Type.InvalidType;

	/**
	 * Database persistent file.
	 */
	private File dbFile = null;
	private com.sparsity.sparksee.gdb.Sparksee sparksee = null;
	private com.sparsity.sparksee.gdb.Database db = null;
	private RemoteTransaction transaction = null;

	public static RemoteGraph open(Map<String, String> properties, File dbFile) throws IOException {
		return new RemoteGraph(properties, dbFile);
	}
	
	public File getDbFile(){
		return dbFile;
	}

	private RemoteGraph(Map<String, String> properties, File dabaseFile) throws IOException {

		URL logback = this.getClass().getClassLoader().getResource("logback.groovy");
		if (logback == null) {
			java.lang.System.out.println("logback.groovy NOT found");
		} else {
			java.lang.System.out.println("logback.groovy found!");
		}

		dbFile = dabaseFile.getCanonicalFile();

		if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
			throw new InvalidParameterException(String.format("Unable to create directory %s.", dbFile.getParent()));
		}

		try {
			File etc = new File("etc");
			if(!etc.exists()){
				etc.mkdirs();
			}
			File tmpCfg = new File(etc, "database.properties");
			FileWriter fw = new FileWriter(tmpCfg);
			
			Set<String> keys = properties.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext()){
				String key = it.next();
				fw.write(key+" = " + properties.get(key)+"\n");
			}
			
			fw.close();

			com.sparsity.sparksee.gdb.SparkseeProperties.load(tmpCfg.getCanonicalPath());

			sparksee = new com.sparsity.sparksee.gdb.Sparksee(new com.sparsity.sparksee.gdb.SparkseeConfig());
			if (!dbFile.exists()) {
				db = sparksee.create(dbFile.getPath(), dbFile.getName());
			} else {
				db = sparksee.open(dbFile.getPath(), false);
			}
			transaction = new RemoteTransaction(db);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public String compute(Long transactionId, String algebra, Map<String, Object> params) {
		if (!((RemoteTransaction) this.tx()).existsSession(transactionId)) {
			return "{\"error\" : \"Invalid transactionid\"}";
		}
		Integer queryId = ((RemoteTransaction) this.tx()).newQuery(transactionId, algebra, params);
		return "{\"id\":" + queryId.toString() + "}";
	}

	public String compute(String algebra, Map<String, Object> params) {
		long timestamp = java.lang.System.currentTimeMillis();
		Long transactionId = ((RemoteTransaction) this.tx()).begin(timestamp);
		Integer queryId = ((RemoteTransaction) this.tx()).newQuery(transactionId, algebra, params);
		timestamp = java.lang.System.currentTimeMillis();
		return "{\"id\":" + queryId.toString() + "}";
	}

	public RemoteTransaction tx() {
		return transaction;
	}

	public String getWS(long transactionId) {
		return ((RemoteTransaction) this.tx()).getWS(transactionId);
	}

	public String garbageCollect(long timestamp) {
		return ((RemoteTransaction) this.tx()).garbageCollect(timestamp);
	}

	public String commit(long transactionId, long timestamp) {
		String commitRequest = ((RemoteTransaction) this.tx()).commit(transactionId, timestamp);
		return commitRequest;
	}

	public String rollback(long transactionId) {
		String rollbackRequest = ((RemoteTransaction) this.tx()).rollback(transactionId);
		return rollbackRequest;
	}

	public String redoWS(long transactionId, long commitTimestamp, long precommitId) {
		return ((RemoteTransaction) this.tx()).redo(transactionId, commitTimestamp, precommitId);
	}

	public String begin(long timestamp) {
		Long transactionId = ((RemoteTransaction) this.tx()).begin(timestamp);
		return "{\"id\":" + transactionId.toString() + "}";
	}

	public String closeQuery(Long queryId) {
		String closeRequest = ((RemoteTransaction) this.tx()).closeQuery(queryId.intValue());
		return closeRequest;
	}

	public String next(Long queryId, Long rows) {
		return ((RemoteTransaction) this.tx()).next(queryId.intValue(), rows.intValue());
	}

	public void close() {
		transaction.closeAll();
		db.close();
		sparksee.close();
	}

	public void shutdown() {
		close();
	}

	public void restart() throws Exception {
		sparksee = new com.sparsity.sparksee.gdb.Sparksee(new com.sparsity.sparksee.gdb.SparkseeConfig());
		if (!dbFile.exists()) {
			db = sparksee.create(dbFile.getPath(), dbFile.getName());
		} else {
			db = sparksee.open(dbFile.getPath(), false);
		}
		transaction = new RemoteTransaction(db);
	}

	public void use(String fileName) throws Exception {
		dbFile = new File(fileName);
		if(dbFile.exists()) {
			if(!db.isClosed()) {
				db.close();
			}
			db = sparksee.open(dbFile.getPath(),false);
			transaction = new RemoteTransaction(db);
		}
	}

	public void runScript(String script, String locale, Map<String,String> variables) throws Exception {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		LoadDataFromScriptsCall call = new LoadDataFromScriptsCall(this, new File(script), locale, variables);
		pool.submit(call);
	}
	
	public void script(String scriptContent, String locale, Map<String,String> variables) throws Exception {
		ExecutorService pool = Executors.newSingleThreadExecutor();
		LoadDataFromScriptsCall call = new LoadDataFromScriptsCall(this, scriptContent, locale, variables);
		pool.submit(call);
	}
}
