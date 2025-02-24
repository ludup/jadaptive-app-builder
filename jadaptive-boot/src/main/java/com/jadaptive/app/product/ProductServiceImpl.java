package com.jadaptive.app.product;

import java.util.Calendar;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.product.Product;
import com.jadaptive.api.product.ProductLogoSource;
import com.jadaptive.api.product.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ApplicationService appService; 
	
	final private Product defaultProduct = new Product() { };
	
	@PostConstruct
	private void postConstruct() {
		I18N.addI18n(Locale.getDefault(), "vendor", "product.name", getProductName());
	}
	
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
			return ApplicationProperties.getValue("app.logo", getProduct().getLogoResource());
		}
	}
	
	@Override
	public String getFaviconResource() { 
		return ApplicationProperties.getValue("app.favicon", getProduct().getFaviconResource());
	}
	
	@Override
	public String getProductName() {
		return ApplicationProperties.getValue("app.name", getProduct().getName());
	}

	@Override
	public String getPoweredBy() {
		return ApplicationProperties.getValue("app.power", getProduct().getVendor());
	}

	@Override
	public String getProductCode() {
		return ApplicationProperties.getValue("app.code", getProduct().getProductCode());
	}

	@Override
	public String getVendor() {
		return ApplicationProperties.getValue("app.vendor", getProduct().getVendor());
	}

	@Override
	public boolean requiresRegistration() {
		return getProduct().requiresRegistration();
	}

	@Override
	public boolean isTenantLicensing() {
		return getProduct().isTenantLicensing();
	}

	@Override
	public boolean isRevenueGenerating() {
		return getProduct().isRevenueGenerating();
	}

	@Override
	public ProductId getProductId() {
		return getProduct().getProductId();
	}
	
	@Override
	public boolean isUserLicensing() {
		return getProduct().isUserLicensing();
	}
	
	private Product getProduct() {
		try {
			Product product = appService.getBean(Product.class);
			return product;
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct;
		}
	}


}
