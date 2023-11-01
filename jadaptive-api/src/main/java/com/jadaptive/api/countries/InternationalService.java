package com.jadaptive.api.countries;

import java.util.Collection;

public interface InternationalService {

	InternationalCode getCode(String country);

	Collection<InternationalCode> getInternationalCodes();

	Collection<Country> getCountries();

	Country getCountry(String country);

	String getCountryName(String country);

	boolean isEU(String country);

}
