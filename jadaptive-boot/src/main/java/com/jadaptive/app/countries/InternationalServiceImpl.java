package com.jadaptive.app.countries;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalCode;
import com.jadaptive.api.countries.InternationalService;

@Service
public class InternationalServiceImpl implements InternationalService {


	private Collection<InternationalCode> internationalCodes = null;
	private Map<String,InternationalCode> codesByCountry = null;
	
	private Collection<Country> countries = null;
	private Map<String,Country> countriesByCode = null;
	
	private static final Set<String> EU_COUNTRIES;

    static {
        Set<String> euCountries = new HashSet<>();
        euCountries.add("AT"); // Austria
        euCountries.add("BE"); // Belgium
        euCountries.add("BG"); // Bulgaria
        euCountries.add("CY"); // Cyprus
        euCountries.add("CZ"); // Czech Republic
        euCountries.add("DE"); // Germany
        euCountries.add("DK"); // Denmark
        euCountries.add("EE"); // Estonia
        euCountries.add("ES"); // Spain
        euCountries.add("FI"); // Finland
        euCountries.add("FR"); // France
        euCountries.add("GR"); // Greece
        euCountries.add("HR"); // Croatia
        euCountries.add("HU"); // Hungary
        euCountries.add("IE"); // Ireland
        euCountries.add("IT"); // Italy
        euCountries.add("LT"); // Lithuania
        euCountries.add("LU"); // Luxembourg
        euCountries.add("LV"); // Latvia
        euCountries.add("MT"); // Malta
        euCountries.add("NL"); // Netherlands
        euCountries.add("PL"); // Poland
        euCountries.add("PT"); // Portugal
        euCountries.add("RO"); // Romania
        euCountries.add("SE"); // Sweden
        euCountries.add("SI"); // Slovenia
        euCountries.add("SK"); // Slovakia

        EU_COUNTRIES = euCountries;
    }
    
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

    public boolean isEU(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        return EU_COUNTRIES.contains(countryCode.toUpperCase());
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
