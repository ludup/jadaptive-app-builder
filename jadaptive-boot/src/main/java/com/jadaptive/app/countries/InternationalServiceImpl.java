package com.jadaptive.app.countries;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalCode;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.db.ClassLoaderService;

@Service
public class InternationalServiceImpl implements InternationalService {


	private Collection<InternationalCode> internationalCodes = null;
	private Map<String,InternationalCode> codesByCountry = null;
	
	private Collection<Country> countries = null;
	private Map<String,Country> countriesByCode = null;
	
	@PostConstruct
	private void postConstruct() {
		
		try {
			internationalCodes = Arrays.asList(new ObjectMapper()
					.readerForArrayOf(InternationalCode.class)
					.readValue(getClass().getResource("/international-codes.json")));

			codesByCountry = new TreeMap<>();
			for(InternationalCode code : internationalCodes) {
				codesByCountry.put(code.getCode(), code);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		try {
			countries = Arrays.asList(new ObjectMapper()
					.readerForArrayOf(Country.class)
					.readValue(getClass().getResource("/countries.json")));

			countriesByCode = new TreeMap<>();
			for(Country code : countries) {
				countriesByCode.put(code.getCode(), code);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		
	}
	
	@Override
	public Country getCountry(String country) {
		return countriesByCode.get(country);
	}
	
	@Override
	public Collection<Country> getCountries() {
		return countries;
	}
	
	@Override
	public InternationalCode getCode(String country) {
		return codesByCountry.get(country);
	}
	
	@Override
	public Collection<InternationalCode> getInternationalCodes() {
		return internationalCodes;
	}

	@Override
	public String getCountryName(String country) {
		Country c = getCountry(country);
		if(Objects.nonNull(c)) {
			return c.getName();
		}
		return "";
	}
}
