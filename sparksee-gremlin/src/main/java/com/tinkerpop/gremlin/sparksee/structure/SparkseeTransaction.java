package com.tinkerpop.gremlin.sparksee.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Transaction;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class SparkseeTransaction implements Transaction {

	private Consumer<Transaction> readWriteConsumer;
	private Consumer<Transaction> closeConsumer;
	private SparkseeGraph graph;
	private com.sparsity.sparksee.gdb.Database db = null;
	private ConcurrentHashMap<Long, Metadata> threadData = new ConcurrentHashMap<Long, Metadata>();
	private ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.Query> queryMap = new ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.Query>();
	private ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.ResultSet> resultMap = new ConcurrentHashMap<Integer, com.sparsity.sparksee.gdb.ResultSet>();
	private ConcurrentHashMap<Long, com.sparsity.sparksee.gdb.Session> sessionMap = new ConcurrentHashMap<Long, com.sparsity.sparksee.gdb.Session>();
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

	protected SparkseeTransaction(SparkseeGraph graph,
			com.sparsity.sparksee.gdb.Database db) {
		this.graph = graph;
		this.db = db;
		readWriteConsumer = READ_WRITE_BEHAVIOR.AUTO;
		closeConsumer = CLOSE_BEHAVIOR.COMMIT;
	}

	protected Long begin(Long timestamp) {
		com.sparsity.sparksee.gdb.Session sess = db.newSession();
		sess.begin();// Mike
		Long transactionId = sessionIdGenerator.incrementAndGet();
		sessionMap.put(transactionId, sess);
		return transactionId;
	}

	protected String commit(Long transactionId, Long timestamp) {
		if (!existsSession(transactionId)) {
			return "{\"error\":\"can not commit an Invalid transaction\"}";
		}
		try {

			com.sparsity.sparksee.gdb.Session sess = sessionMap
					.get(transactionId);
			sess.commit();
			sess.close();
			sessionMap.remove(transactionId); // Mike
			return "{}";
		} catch (Exception e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	protected String redo(Long transactionId, Long timestamp) {
		if (!existsSession(transactionId)) {
			db.redoPrecommitted(transactionId);
			return "{}";
		} else {
			return commit(transactionId, timestamp);
		}

	}

	public String getWS(Long transactionId) {

		if (existsSession(transactionId)) {
			com.sparsity.sparksee.gdb.Session sess = sessionMap
					.get(transactionId);
			// sess.preCommit();
		}

		return "{\"id\":" + transactionId.toString() + "}";
	}

	protected String rollback(Long transactionId) {
		try {
			if (!existsSession(transactionId)) {
				return "{\"error\":\"can not rollback an Invalid transaction\"}";
			}
			com.sparsity.sparksee.gdb.Session sess = sessionMap
					.get(transactionId);
			sess.rollback();
			sess.close();
			sessionMap.remove(transactionId);
			return "{}";
		} catch (Exception e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	protected Boolean existsSession(Long transactionId) {
		return sessionMap.containsKey(transactionId);
	}

	protected Integer newQuery(Long transactionId, String algebra,
			Map<String, Object> params) {

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
				//resultMap.get(queryId).close();
				resultMap.remove(queryId);
			}
			//queryMap.get(queryId).close();
			queryMap.remove(queryId);
			/*
			 * long timestamp = java.lang.System.currentTimeMillis();// Mike
			 * if(querySessionMap.containsKey(queryId)){ ((SparkseeTransaction)
			 * this).commit(querySessionMap.get(queryId), timestamp);// Mike }
			 */
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
				throw new IllegalStateException(
						"Transaction has not been started");
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
		if (closeConsumer == CLOSE_BEHAVIOR.MANUAL) {
			throw Transaction.Exceptions.openTransactionsOnClose();
		}
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
			if (closeConsumer == CLOSE_BEHAVIOR.COMMIT) {
				md.session.commit();
			} else if (closeConsumer == CLOSE_BEHAVIOR.ROLLBACK) {
				md.session.rollback();
			}
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

	@Override
	public void open() {
		if (isOpen()) {
			throw Transaction.Exceptions.transactionAlreadyOpen();
		}

		Long threadId = Thread.currentThread().getId();
		Metadata metadata = new Metadata();
		metadata.session = db.newSession();
		threadData.put(threadId, metadata);
		if (writeMode.get()) {
			threadData.get(threadId).session.beginUpdate();
		} else {
			threadData.get(threadId).session.begin();
		}
	}

	@Override
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

	@Override
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

	@Override
	public <R> Workload<R> submit(final Function<Graph, R> work) {
		return new Workload<>(graph, work);
	}

	@Override
	public <G extends Graph> G create() {
		throw Transaction.Exceptions.threadedTransactionsNotSupported();
	}

	@Override
	public boolean isOpen() {
		Long threadId = Thread.currentThread().getId();
		return threadData.containsKey(threadId);
	}

	public void write() {
		writeMode.set(true);
	}

	@Override
	public void readWrite() {
		this.readWriteConsumer.accept(this);
	}

	@Override
	public void close() {
		this.closeConsumer.accept(this);
	}

	@Override
	public Transaction onReadWrite(final Consumer<Transaction> consumer) {
		this.readWriteConsumer = Optional.ofNullable(consumer).orElseThrow(
				Transaction.Exceptions::onReadWriteBehaviorCannotBeNull);
		return this;
	}

	@Override
	public Transaction onClose(final Consumer<Transaction> consumer) {
		this.closeConsumer = Optional.ofNullable(consumer).orElseThrow(
				Transaction.Exceptions::onCloseBehaviorCannotBeNull);
		return this;
	}
}
