package com.jadaptive.app.db;

import org.springframework.stereotype.Service;

import com.jadaptive.api.db.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

	protected final DocumentDatabase db;
	
	public TransactionServiceImpl(DocumentDatabase db) {
		this.db = db;
	}
	
	@Override
	public void executeTransaction(Runnable r) {
		db.doInTransaction(r);
	}
}
