package com.jadaptive.app.product;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.product.Product;
import com.jadaptive.api.product.ProductLogoSource;
import com.jadaptive.api.product.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ApplicationService appService; 
	
	public String getVersion() {
		return ApplicationVersion.getVersion();
	}

	@Override
	public String getCopyright() {
		return String.format("&copy; 2002-%s Jadaptive Limited", Calendar.getInstance().get(Calendar.YEAR));
	}
	
	@Override
	public String getLogoResource() {
		try {
			ProductLogoSource source = appService.getBean(ProductLogoSource.class);
			return source.getProductLogo();
		} catch(Throwable e) {
			Product product = appService.getBean(Product.class);
			return product.getLogoResource();
		}
	}
	
	@Override
	public String getFaviconResource() {
		Product product = appService.getBean(Product.class);
		return product.getFaviconResource();
	}
	
	@Override
	public String getProductName() {
		Product product = appService.getBean(Product.class);
		return product.getName();	
	}

	@Override
	public String getPoweredBy() {
		Product product = appService.getBean(Product.class);
		return product.getPoweredBy();
	}

}
