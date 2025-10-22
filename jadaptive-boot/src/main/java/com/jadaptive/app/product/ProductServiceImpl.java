package com.jadaptive.app.product;

import java.util.Calendar;
import java.util.Locale;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.product.Product;
import com.jadaptive.api.product.ProductLogoSource;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.ui.pages.ext.Base64Images;

@Service
public class ProductServiceImpl implements ProductService, StartupAware {

	@Autowired
	private App appService; 

	@Autowired
	private Base64Images base64Images;
	
	final private Product defaultProduct = new Product() { };
	
	public String getVersion() {
		return ApplicationVersion.getVersion();
	}

	@Override
	public String getCopyright() {
		return String.format("&copy; 2002-%s %s", 
				Calendar.getInstance().get(Calendar.YEAR),
				getVendor());
	}
	
	@Override
	public String getLogoResource(ImageURIFormat uriFormat) {
		try {
			return base64Images.encodeToString(uriFormat, appService.getBean(ProductLogoSource.class).getProductLogo());
		} catch(Throwable e) {
			return base64Images.encodeToString(uriFormat, ApplicationProperties.getValue("app.logo", getProduct().getLogoResource()));
		}
	}
	
	@Override
	public boolean supportsPAYG() {
		return getProduct().supportsPAYG();
	}
	
	@Override
	public String getFaviconResource(ImageURIFormat uriFormat) { 
		return base64Images.encodeToString(uriFormat, ApplicationProperties.getValue("app.favicon", getProduct().getFaviconResource()));
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
	
	private Product getProduct() {
		try {
			Product product = appService.getBean(Product.class);
			return product;
		} catch (NoSuchBeanDefinitionException e1) {
			return defaultProduct;
		}
	}

	@Override
	public void onApplicationStartup() {
		I18N.addI18n(Locale.getDefault(), "vendor", "product.name", getProductName());
	}


}
