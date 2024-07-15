package com.jadaptive.api.ui;

import java.util.Collection;
import java.util.Optional;

public interface HomePageResolver {
	
	int getWeight();

	Collection<String> getPermissions();
	
	Optional<Class<? extends Page>> resolve();
}
