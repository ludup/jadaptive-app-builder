package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.tenant.TenantService;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private static Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
	
	private final ThreadLocal<TX> tx = new ThreadLocal<>();
	
	@Autowired
	private TenantService tenantService; 
	
	private ThreadLocal<List<Runnable>> completionTasks = ThreadLocal.withInitial(()->new ArrayList<>());
	
	protected final DocumentDatabase db;
	
	public TransactionServiceImpl(DocumentDatabase db) {
		this.db = db;
	}
	 
	@Override
	public TX tx() {
		var tx = this.tx.get();
		if(tx == null)
			throw new IllegalStateException("Not in a transaction.");
		return tx;
	}
	
	@Override
	public void executeOrDelayUntilCommitted(Runnable r) {
		if(!isTransactionActive()) {
			r.run();
		} else {
			completionTasks.get().add(r);
		}
	}
	
	@Override
	public void executeTransaction(Runnable r) {
		var rollbacks = new ArrayList<ThrowingRunnable>();
		var tx = new TXImpl(rollbacks);
		this.tx.set(tx);
		try {
			db.doInTransaction(tenantService.getCurrentTenant().getUuid(), r);
			for(Runnable task : completionTasks.get()) {
				try {
					task.run();
				} catch(Throwable e) {
					log.error("Transaction completion task failed with exception", e);
				}
			}
		}
		catch(RuntimeException e) {
			try {
				for(int i = rollbacks.size() - 1 ; i >= 0 ; i--) {
					rollbacks.get(i).run();
				}
			}
			catch(Exception e2) {
				log.warn("Transaction failed, and rollback failed.", e2);
			} 
			throw e;
		}
		finally {
			this.completionTasks.remove();
			this.tx.remove();
		}
	}

	@Override
	public boolean isTransactionActive() {
		return db.isTransactionActive();
	}
	
	private final static class TXImpl implements TX {

		private List<ThrowingRunnable> rollbacks;

		private TXImpl(List<ThrowingRunnable> rollbacks) {
			this.rollbacks = rollbacks;
		}

		@Override
		public void undoable(ThrowingRunnable rollback) {
			rollbacks.add(rollback);
		}
		
	}
}
