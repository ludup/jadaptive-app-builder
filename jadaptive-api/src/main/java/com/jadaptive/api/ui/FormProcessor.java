package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;

public interface FormProcessor<T> {

	Class<T> getFormClass();

}
