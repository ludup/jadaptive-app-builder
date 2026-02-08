package com.jadaptive.api.user;

import java.time.Instant;

public interface UserImportService {

	public void importUsers(Instant lastImport, ImportFeedback feedback);
	
	interface ImportFeedback {
		
		void importCount(long count);
		
		void userImported(User user);
		
		void userUpdated(User user);
		
		void userDeleted(User user);

		void progress(String string);

		void progress(String bundle, String key, String... args);
		
	}
}
