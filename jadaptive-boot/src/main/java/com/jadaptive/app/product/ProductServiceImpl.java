package com.jadaptive.app.product;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.product.Product;
import com.jadaptive.api.product.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ApplicationService appService; 
	
	final List<String> DEFAULT_FEATURES = Arrays.asList("sshd");
	
	@Override
	public boolean supportsFeature(String feature) {
	
		boolean supports = supportsDefault(feature);
		if(!supports) {
			Product product = appService.getBean(Product.class);
			if(Objects.nonNull(product)) {
				return product.supportsFeature(feature);
			}
		}
		return supports;
	}

	private boolean supportsDefault(String feature) {
		return DEFAULT_FEATURES.contains(feature);
	}
}
