package com.jadaptive.api.ui;

import java.util.List;

public interface AuthenticationFlow {

	List<Class<? extends Page>> getAuthenticators();

}
