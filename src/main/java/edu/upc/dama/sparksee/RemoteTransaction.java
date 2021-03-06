package edu.upc.dama.sparksee;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class RemoteTransaction {

	private com.sparsity.sparksee.gdb.Database db = null;
	private ConcurrentHashMap<Long, Metadata> threadData = new ConcurrentHashMap<Long, Metadata>();
	private ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.Query> queryMap = new ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.Query>();
	private ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.ResultSet> resultMap = new ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.ResultSet>();
	private ConcurrentHashMap<Long, com.sparsity.sparksee.gdb.Session> sessionMap = new ConcurrentHashMap<Long, com.sparsity.sparksee.gdb.Session>();
	private ConcurrentHashMap<Long, Long> timestampsMap = new ConcurrentHashMap<Long, Long>();

	private ConcurrentHashMap<Integer, Long> querySessionMap = new ConcurrentHashMap<Integer, Long>();
	private AtomicInteger queryIdGenerator = new AtomicInteger(0);
	private AtomicLong sessionIdGenerator = new AtomicLong(0);

	private ThreadLocal<Boolean> writeMode = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private class Metadata {
		com.sparsity.sparksee.gdb.Session session = null;
		List<com.sparsity.sparksee.gdb.Objects> collection = new ArrayList<com.sparsity.sparksee.gdb.Objects>();
	};

	protected RemoteTransaction(com.sparsity.sparksee.gdb.Database db) {
		this.db = db;
	}

	protected Long begin(Long timestamp) {
		com.sparsity.sparksee.gdb.Session sess = db.newSession();
		sess.begin();
		Long transactionId = sessionIdGenerator.incrementAndGet();
		sessionMap.put(transactionId, sess);
		timestampsMap.put(transactionId, timestamp);
		return transactionId;
	}

	protected String commit(Long transactionId, Long timestamp) {
		if (!existsSession(transactionId)) {
			return "{\"error\":\"can not commit an Invalid transaction\"}";
		}
		try {

			com.sparsity.sparksee.gdb.Session sess = sessionMap.get(transactionId);
			sess.commit();
			sess.close();
			sessionMap.remove(transactionId); // Mike
			timestampsMap.remove(transactionId);
			return "{}";
		} catch (Exception e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	protected String redo(Long transactionId, Long timestamp, Long precommitId) {
		if (!existsSession(transactionId)) {
			db.redoPrecommitted(precommitId);
			return "{}";
		} else {
			return commit(transactionId, timestamp);
		}

	}

	public String getWS(Long transactionId) {

		Long precommitId = 0L;
		if (existsSession(transactionId)) {
			com.sparsity.sparksee.gdb.Session sess = sessionMap.get(transactionId);
			precommitId = sess.preCommit();
		}

		return "{\"id\":" + transactionId.toString() + ", \"precommitId\":" + precommitId + "}";
	}

	protected String rollback(Long transactionId) {
		try {
			if (!existsSession(transactionId)) {
				return "{\"error\":\"can not rollback an Invalid transaction\"}";
			}
			com.sparsity.sparksee.gdb.Session sess = sessionMap.get(transactionId);
			sess.rollback();
			sess.close();
			sessionMap.remove(transactionId);
			timestampsMap.remove(transactionId);
			return "{}";
		} catch (Exception e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	public String garbageCollect(Long timestamp) {
		String result = "";
		Set<Entry<Long, Long>> entries = timestampsMap.entrySet();
		Iterator<Entry<Long, Long>> it = entries.iterator();
		Long txId = null;

		while (it.hasNext() && txId == null) {
			Entry<Long, Long> entry = it.next();
			if (entry.getValue().equals(timestamp)) {
				txId = entry.getKey();
			}
		}
		if (txId != null) {
			result = "{ \"tx\": \"" + txId.toString() + "\", \"timestamp\": \"" + timestamp + "\" }";
		} else {
			result = "{ \"timestamp\": \"" + timestamp + "\" }";
		}
		return result;
	}

	protected Boolean existsSession(Long transactionId) {
		return sessionMap.containsKey(transactionId);
	}

	protected Integer newQuery(Long transactionId, String algebra, Map<String, Object> params) {

		com.sparsity.sparksee.gdb.Session sess = sessionMap.get(transactionId);
		com.sparsity.sparksee.gdb.Query q = sess.newQuery();
		// q.setDynamic(arg0, arg1); passem parametres query
		com.sparsity.sparksee.gdb.ResultSet rs = q.execute(algebra);
		Integer queryId = queryIdGenerator.incrementAndGet();
		queryMap.put(queryId, q);
		resultMap.put(queryId, rs);
		querySessionMap.put(queryId, transactionId);
		return queryId;
	}

	protected String next(Integer queryId, Integer rows) {
		if (queryMap.containsKey(queryId)) {
			if (!sessionMap.containsKey(querySessionMap.get(queryId))) {
				return "{\"error\": \"using a query of already closed transaction\"}";
			}
			com.sparsity.sparksee.gdb.ResultSet rs = resultMap.get(queryId);
			String result = rs.getJSON(rows);
			return result;
		}
		return "";
	}

	protected String closeQuery(Integer queryId) {
		if (queryMap.containsKey(queryId)) {
			if (resultMap.containsKey(queryId)) {
				resultMap.get(queryId).close();
				resultMap.remove(queryId);
			}
			queryMap.get(queryId).close();
			queryMap.remove(queryId);

			querySessionMap.remove(queryId);
			return "{}";
		}
		return "{\"ERROR\":\"Unexisting query\"}";
	}

	/**
	 * Gets the Sparksee Session
	 *
	 * @return The Sparksee Session
	 */
	protected com.sparsity.sparksee.gdb.Session getRawSession(boolean exception) {
		Long threadId = Thread.currentThread().getId();
		if (!threadData.containsKey(threadId)) {
			if (exception) {
				throw new IllegalStateException("Transaction has not been started");
			}
			return null;
		}
		return threadData.get(threadId).session;
	}

	/**
	 * Gets the Sparksee raw graph.
	 *
	 * @return Sparksee raw graph.
	 */
	protected com.sparsity.sparksee.gdb.Graph getRawGraph() {
		com.sparsity.sparksee.gdb.Session sess = getRawSession(false);
		if (sess == null) {
			throw new IllegalStateException("Transaction has not been started");
		}
		return sess.getGraph();
	}

	protected void closeAll() {

		for (com.sparsity.sparksee.gdb.ResultSet rs : resultMap.values()) {
			rs.close();
		}
		for (com.sparsity.sparksee.gdb.Query query : queryMap.values()) {
			query.close();
		}
		for (com.sparsity.sparksee.gdb.Session sess : sessionMap.values()) {
			sess.close();
		}

		for (Metadata md : threadData.values()) {

			for (com.sparsity.sparksee.gdb.Objects objs : md.collection) {
				objs.close();
			}

			md.session.commit();

			md.session.close();
		}
		threadData.clear();
	}

	public boolean arePendingTransactions() {
		if (!threadData.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	protected void add(com.sparsity.sparksee.gdb.Objects objs) {
		if (!isOpen()) {
			return;
		}
		Long threadId = Thread.currentThread().getId();
		Metadata metadata = threadData.get(threadId);
		metadata.collection.add(objs);
	}

	protected void remove(com.sparsity.sparksee.gdb.Objects objs) {
		if (!isOpen()) {
			return;
		}
		Long threadId = Thread.currentThread().getId();
		Metadata metadata = threadData.get(threadId);
		metadata.collection.remove(objs);
	}

	public void open() {
		if (isOpen()) {
			throw new RuntimeException("Transaction already open");
		}

		Long threadId = Thread.currentThread().getId();
		Metadata metadata = new Metadata();
		metadata.session = db.newSession();
		threadData.put(threadId, metadata);
		if (writeMode.get()) {
			threadData.get(threadId).session.begin();
		} else {
			threadData.get(threadId).session.begin();
		}
	}

	public void commit() {
		writeMode.set(false);
		if (!isOpen()) {
			return;
		}

		Long threadId = Thread.currentThread().getId();
		Metadata metadata = threadData.get(threadId);
		metadata.session.commit();
		for (com.sparsity.sparksee.gdb.Objects objs : metadata.collection) {
			objs.close();
		}
		metadata.session.close();
		threadData.remove(threadId);
	}

	public void rollback() {
		writeMode.set(false);
		if (!isOpen()) {
			return;
		}

		Long threadId = Thread.currentThread().getId();
		Metadata metadata = threadData.get(threadId);
		metadata.session.rollback();
		for (com.sparsity.sparksee.gdb.Objects objs : metadata.collection) {
			objs.close();
		}
		metadata.session.close();
		threadData.remove(threadId);
	}

	public boolean isOpen() {
		Long threadId = Thread.currentThread().getId();
		return threadData.containsKey(threadId);
	}

	public void write() {
		writeMode.set(true);
	}

}
