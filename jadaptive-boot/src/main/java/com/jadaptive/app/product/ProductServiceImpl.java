package com.jadaptive.app.product;

import java.util.Calendar;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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
	
	final private Product defaultProduct = new Product() { };
	
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
			try {
				Product product = appService.getBean(Product.class);
				return product.getLogoResource();
			} catch (NoSuchBeanDefinitionException e1) {
				return defaultProduct.getLogoResource();
			}
		}
	}
	
	@Override
	public String getFaviconResource() {
		try {
			Product product = appService.getBean(Product.class);
			return product.getFaviconResource();
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct.getFaviconResource();
		}
	}
	
	@Override
	public String getProductName() {
		try {
			Product product = appService.getBean(Product.class);
			return product.getName();
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct.getName();
		}
	}

	@Override
	public String getPoweredBy() {
		try {
			Product product = appService.getBean(Product.class);
			return product.getPoweredBy();
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct.getPoweredBy();
		}
	}

	@Override
	public String getProductCode() {
		try {
			Product product = appService.getBean(Product.class);
			return product.getProductCode();
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct.getProductCode();
		}
	}

}
