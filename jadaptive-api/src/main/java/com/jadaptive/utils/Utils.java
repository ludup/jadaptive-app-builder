package com.jadaptive.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

public class Utils {

	public static final int ONE_MINUTE = 60000;
	public static final int ONE_HOUR = ONE_MINUTE * 60;
	public static final int ONE_DAY = ONE_HOUR * 24;

	static Logger log = LoggerFactory.getLogger(Utils.class);

	static SecureRandom random = new SecureRandom();
	
	/**
	 * Encapsulate a part of a string by a given character.useful in hiding part of a password
	 * 
	 * @param original
	 *            Original string
	 * @param start
	 *            Start position of masking
	 * @param maskcharacter
	 *            masking character of the rest of the String
	 * @return if the given string length is shorter than start position then
	 *         return the original string, otherwise return original string with
	 *         replace masking character from the start position onward
	 */
	public static String maskingString(String original, int start,String maskcharacter) {
		String result = null;
		if (start < 0) {
			throw new IllegalArgumentException("Start position should be greater than 0");
		}
		if (original.length() > start) {
			result = original.substring(0, start)+ original.substring(start).replaceAll(".", maskcharacter);
		} else {
			result = original;
		}
		return result;
	}
	
	/**
	 * Format a date with a given format. Formats are cached to prevent excessive use of 
	 * DateFormat.
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	
	public static String formatDate(Date date) {
		return formatDate(date, "MMM d, yyyy");
	}
	
 	public static String formatDateTime(Long date) {
		return formatDateTime(new Date(date));
	}
	
	public static String formatDateTime(Date date) {
		return formatDate(date, "EEE, d MMM yyyy HH:mm:ss.SSS Z");
	}
	
	public static String formatShortDate(Date date) {
		return formatDate(date, "EEE, d MMM yyyy");
	}
	
	public static String formatShortDate(long date) {
		return formatShortDate(new Date(date));
	}
	
	/**
	 * Parse a date on a given format. 
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date, String format) {
		if(Objects.isNull(date)) {
			return null;
		}
		try {
			return new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static Date parseDateTime(String date) {
		if(Objects.isNull(date)) {
			return null;
		}
		try {
			return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss.SSS Z").parse(date);
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static Date parseShortDate(String date) {
		if(Objects.isNull(date)) {
			return null;
		}
		try {
			return new SimpleDateFormat("EEE, d MMM yyyy").parse(date);
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static Date today() {
		return todayCalendar().getTime();
	}
	
	public static Calendar todayCalendar() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}
	
	public static Calendar tomorrowCalendar() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, 1);
		
		return date;
	}
	
	public static Date tomorrow() {
		return tomorrowCalendar().getTime();
	}
	
	public static Date yesterday() {
		
		return yesterdayCalendar().getTime();
	}
	

	public static Calendar yesterdayCalendar() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, -1);
		
		return date;
	}


	/**
	 * Strip the port from a host header.
	 * @param hostHeader
	 * @return
	 */
	public static String stripPort(String hostHeader) {
		return before(hostHeader, ":");
	}

	public static String after(String value, String string) {
		int idx = value.indexOf(string);
		if(idx > -1) {
			return value.substring(idx+1);
		}
		return "";
	}
	
	
	public static String before(String value, String string) {
		int idx = value.indexOf(string);
		if(idx > -1) {
			return value.substring(0,idx);
		}
		return value;
	}
	
	public static String base64Encode(byte[] bytes) {
		return java.util.Base64.getEncoder().encodeToString(bytes);
	}
	
	public static String base64Encode(String resourceKey) {
		try {
			return base64Encode(resourceKey.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}

	public static byte[] base64Decode(String property) throws IOException {
		return java.util.Base64.getDecoder().decode(property.getBytes("UTF-8"));
	}

	public static String format(Double d) {
		return new DecimalFormat("0.00").format(d);
	}
	
	public static String format(Float f) {
		return new DecimalFormat("0.00").format(f);
	}
	
	public static String prettyPrintXml(SOAPMessage message) throws SOAPException, IOException, TransformerFactoryConfigurationError, TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		
		return prettyPrintXml(out.toString("UTF-8"));
	}
	
	public static String prettyPrintXml(String unformattedXml) throws TransformerFactoryConfigurationError, UnsupportedEncodingException, TransformerException {

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(
				new StreamSource(new ByteArrayInputStream(unformattedXml.getBytes("UTF-8"))),
				result);
		return result.getWriter().toString();
	}
	
	public static String checkNull(String str) {
		if(str==null) {
			return "";
		}
		return str;
	}
	
	public static String checkNull(String str, String def) {
		if(str==null) {
			return def;
		}
		return str;
	}

	public static String urlEncode(String message) {
		try {
			return URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}
	
	public static String[] urlDecodeAll(String... message) {
		String[] a = new String[message.length];
		for(int i = 0 ; i < a.length ; i++)
			a[i] = urlDecode(message[i]);
		return a;
	}
	
	public static String urlDecode(String message) {
		try {
			return URLDecoder.decode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}

	public static boolean isUUID(String attachment) {
		 try {
			UUID.fromString(attachment);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String stripQuery(String url) {
		int idx = url.indexOf('?');
		if(idx > -1) {
			url = url.substring(0,  idx);
		}
		return url;
	}
	
	public static String generateRandomAlphaNumericString(int length) {
	    return new BigInteger(length * 8, random).toString(32).substring(0,  length);
	}


	public static <T> List<T> nullSafe(List<T> list){
		if(list == null){
			return Collections.<T>emptyList();
		}
		return list;
	}

	public static <T> Set<T> nullSafe(Set<T> set){
		if(set == null){
			return Collections.<T>emptySet();
		}
		return set;
	}
	
	public static String csv(String delim, String[] items) {
		StringBuffer b = new StringBuffer();
		for(String i : items) {
			if(b.length() > 0) {
				b.append(delim);
			}
			b.append(i);
		}
		return b.toString();
	}
	
	public static <T> String csv(Collection<T> items) {
		StringBuffer b = new StringBuffer();
		for(T i : items) {
			if(b.length() > 0) {
				b.append(",");
			}
			b.append(i.toString());
		}
		return b.toString();
	}
	
	public static String csv(String... items) {
		return csv(Arrays.asList(items));
	}
	
	public static String csv(Object[] items) {
		StringBuffer b = new StringBuffer();
		for(Object i : items) {
			if(b.length() > 0) {
				b.append(",");
			}
			b.append(i.toString());
		}
		return b.toString();
	}

	public static String encodeURIPath(String url) {
		return UriUtils.encodePath(url, "UTF-8");
	}

	public static Date thirtyDays() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, 30);
		
		return date.getTime();
	}

	public static byte[] getUTF8Bytes(String str) {
		try {
			return str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static Date convertToUTC(Date date, TimeZone tz) {
		return new Date(date.getTime() + tz.getOffset(date.getTime()));
	}

	public static String stripHostname(String uri) {
		return after(uri, ":");
	}

	public static List<String> fromCsv(String value) {
		return new ArrayList<String>(Arrays.asList(value.split(",")));
	}

	public static int parseIntOrDefault(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			return 0;
		}
	}
}
