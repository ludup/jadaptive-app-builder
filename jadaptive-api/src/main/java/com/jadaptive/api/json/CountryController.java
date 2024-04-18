package com.jadaptive.api.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.lang.model.UnknownEntityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.countries.Country;
import com.jadaptive.api.countries.InternationalService;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.session.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CountryController extends BootstrapTableController<Country> {

	private static final String COUNTRY_PAGE = "countryPage";
	
	@Autowired
	private InternationalService internationalService; 
	
	@RequestMapping(value="/app/api/countries/table", method = { RequestMethod.POST, RequestMethod.GET }, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public BootstrapTableResult<Country> tableCountries(HttpServletRequest request, 
			@RequestParam(required=false, defaultValue = "asc") String order,
			@RequestParam(required=false, defaultValue = "0") int offset,
			@RequestParam(required=false, defaultValue = "100") int limit) throws RepositoryException, UnknownEntityException, ObjectException {
		
		try {
			
			return processDataTablesRequest(request, 
//					template,
				new BootstrapTablePageProcessor() {

					@Override
					public Collection<?> getPage(String searchColumn, String searchPattern, int start,
							int length, String sortBy)
							throws UnauthorizedException,
							AccessDeniedException {
						length = (int) Math.min( internationalService.getCountries().size() - start, length);
						List<Country> results = new ArrayList<>();
						
						for(Country con : internationalService.getCountries()) {
							if(con.getName().contains(searchPattern)) {
								results.add((Country)con);
							}
						}
						List<Country> page = Arrays.asList(Arrays.copyOfRange(results.toArray(new Country[0]), start, Math.min(length, results.size()-start)));
						request.setAttribute(COUNTRY_PAGE, page);
						return results;
					}

					@Override
					public Long getTotalCount(String searchColumn, String searchPattern)
							throws UnauthorizedException,
							AccessDeniedException {
						return (long) ((Collection<?>)request.getAttribute(COUNTRY_PAGE)).size();
					}
				});

		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("GET api/countrues/table", e);
			}
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
