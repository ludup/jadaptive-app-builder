package com.jadaptive.app.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jadaptive.api.ui.UriRedirect;

@ControllerAdvice
public class RedirectController 
  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { UriRedirect.class })
    protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) throws URISyntaxException {
    	HttpHeaders headers = new HttpHeaders();
    	headers.setLocation(new URI(((UriRedirect)ex).getUri()));
    	return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}