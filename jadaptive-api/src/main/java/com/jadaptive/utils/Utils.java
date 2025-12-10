package com.jadaptive.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import com.jadaptive.api.repository.NamedDocument;

public class Utils {

	public static final int ONE_MINUTE = 60000;
	public static final int ONE_HOUR = ONE_MINUTE * 60;
	public static final int ONE_DAY = ONE_HOUR * 24;

	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	public static final String HTTP_URL_PATTERN = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$";
	public static final String TIME_24_PATTERN = "^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$";

	public static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String PHONE_PATTERN = "^[+]*[-\\s\\.\\d()]*$";

	static Logger log = LoggerFactory.getLogger(Utils.class);

	static SecureRandom random = new SecureRandom();

	/**
	 * Encapsulate a part of a string by a given character.useful in hiding part of
	 * a password
	 * 
	 * @param original      Original string
	 * @param start         Start position of masking
	 * @param maskcharacter masking character of the rest of the String
	 * @return if the given string length is shorter than start position then return
	 *         the original string, otherwise return original string with replace
	 *         masking character from the start position onward
	 */
	public static String maskingString(String original, int start, String maskcharacter) {
		String result = null;
		if (start < 0) {
			throw new IllegalArgumentException("Start position should be greater than 0");
		}
		if (original.length() > start) {
			result = original.substring(0, start) + original.substring(start).replaceAll(".", maskcharacter);
		} else {
			result = original;
		}
		return result;
	}

	/**
	 * Format a date with a given format. Formats are cached to prevent excessive
	 * use of DateFormat.
	 * 
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

	public static String formatTimestamp(Date date) {
		return formatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	}

	public static Date parseTimestamp(String date) {
		try {
			return parseDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		} catch (IllegalStateException e) {
			try {
				return parseDate(date, "yyyy-MM-dd HH:mm:ss.SSS");
			} catch (IllegalStateException e2) {
				return parseDate(date, "yyyy-MM-dd HH:mm:ss");
			}
		}
	}

	public static String formatISODate(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	public static String formatShortDate(Date date) {
		return formatDate(date, "EEE, d MMM yyyy");
	}

	public static String formatShortDate(long date) {
		return formatShortDate(new Date(date));
	}

	/**
	 * Parse a date on a given format.
	 * 
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date, String format) {
		if (Objects.isNull(date)) {
			return null;
		}
		try {
			return new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static Date parseDateTime(String date) {
		if (Objects.isNull(date)) {
			return null;
		}
		try {
			return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss.SSS Z").parse(date);
		} catch (ParseException e) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
			} catch (ParseException e1) {
				throw new IllegalStateException(e.getMessage(), e1);
			}

		}
	}

	public static Date parseShortDate(String date) {
		if (Objects.isNull(date)) {
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

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}
	
	public static Calendar thisWeekCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return date;
	}
	
	public static Date thisWeek() {
		return thisWeekCalendar().getTime();
	}
	
	public static Calendar thisMonthCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.DAY_OF_MONTH, 1);
		return date;
	}
	
	public static Calendar thisYearCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.MONTH, 1);
		return date;
	}
	
	public static Date thisYear() {
		return thisYearCalendar().getTime();
	}
	
	public static Date thisMonth() {
		return thisMonthCalendar().getTime();
	}
	
	public static Date lastMonth() {
		return DateUtils.addMonths(thisMonth(), -1);
	}
	
	public static Date lastWeek() {
		return DateUtils.addWeeks(thisWeek(), -1);
	}
	
	public static Date lastYear() {
		return DateUtils.addMonths(thisYear(), -1);
	}
	
	public static Date nextMonth() {
		return DateUtils.addMonths(thisMonth(), 1);
	}
	

	public static Calendar tomorrowCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
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

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		date.add(Calendar.DAY_OF_MONTH, -1);

		return date;
	}

	/**
	 * Strip the port from a host header.
	 * 
	 * @param hostHeader
	 * @return
	 */
	public static String stripPort(String hostHeader) {
		return before(hostHeader, ":");
	}

	public static String after(String value, String string) {
		int idx = value.indexOf(string);
		if (idx > -1 && idx + string.length() < value.length()) {
			return value.substring(idx + string.length());
		}
		return "";
	}

	public static String before(String value, String string) {
		int idx = value.indexOf(string);
		if (idx > -1) {
			return value.substring(0, idx);
		}
		return value;
	}

	public static String beforeLast(String value, String string) {
		int idx = value.lastIndexOf(string);
		if (idx > -1) {
			return value.substring(0, idx);
		}
		return value;
	}

	public static String afterLast(String value, String string) {
		int idx = value.lastIndexOf(string);
		if (idx > -1 && idx + string.length() < value.length()) {
			return value.substring(idx + string.length());
		}
		return "";
	}
	
	public static boolean isIPAddress(String ip) {
		return IPAddressValidator.getInstance().validate(ip);
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

	public static String base64DecodeToString(String str) {
		try {
			return new String(base64Decode(str), "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}

	public static String format(Double d) {
		return new DecimalFormat("0.00").format(d);
	}

	public static String format(Float f) {
		return new DecimalFormat("0.00").format(f);
	}

	public static String prettyPrintXml(SOAPMessage message)
			throws SOAPException, IOException, TransformerFactoryConfigurationError, TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);

		return prettyPrintXml(out.toString("UTF-8"));
	}

	public static String prettyPrintXml(String unformattedXml)
			throws TransformerFactoryConfigurationError, UnsupportedEncodingException, TransformerException {

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(new StreamSource(new ByteArrayInputStream(unformattedXml.getBytes("UTF-8"))), result);
		return result.getWriter().toString();
	}

	public static String checkNull(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}

	public static String checkNullToString(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}

	public static String checkNull(String str, String def) {
		if (str == null) {
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
		for (int i = 0; i < a.length; i++)
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
		if (idx > -1) {
			url = url.substring(0, idx);
		}
		return url;
	}

	public static String generateRandomAlphaNumericString(int length) {
		return random.ints(0, ALLOWED_CHARACTERS.length()).limit(length)
				.mapToObj(x -> ALLOWED_CHARACTERS.substring(x, x + 1)).collect(Collectors.joining());
	}

	public static String generateRandomNumericString(int length) {
		return String.join("",
				random.ints(0, 10).limit(length).mapToObj(x -> String.valueOf(x)).collect(Collectors.toList()));
	}

	public static <T> List<T> nullSafe(List<T> list) {
		if (list == null) {
			return Collections.<T>emptyList();
		}
		return list;
	}

	public static <T> Set<T> nullSafe(Set<T> set) {
		if (set == null) {
			return Collections.<T>emptySet();
		}
		return set;
	}

	public static String csv(String delim, String[] items) {
		StringBuffer b = new StringBuffer();
		for (String i : items) {
			if (b.length() > 0) {
				b.append(delim);
			}
			b.append(i);
		}
		return b.toString();
	}

	public static <T> String csv(Collection<T> items) {
		StringBuffer b = new StringBuffer();
		for (T i : items) {
			if (b.length() > 0) {
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
		for (Object i : items) {
			if (b.length() > 0) {
				b.append(",");
			}
			if (Objects.nonNull(i)) {
				b.append(i.toString());
			}
		}
		return b.toString();
	}

	public static String getBaseURL(String url) {
		String protocol = before(url, "//");
		String tmp = after(url, "//");

		return protocol + "//" + before(tmp, "/");
	}

	public static String encodeURIPath(String url) {
		return UriUtils.encodePath(url, "UTF-8");
	}

	public static Calendar thirtyDaysCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		date.add(Calendar.DAY_OF_MONTH, 30);

		return date;

	}

	public static Date thirtyDays() {
		return thirtyDaysCalendar().getTime();
	}

	public static Calendar thirtyDaysAgoCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		date.add(Calendar.DAY_OF_MONTH, -30);

		return date;

	}

	public static Date sevenDaysAgo() {
		return sevenDaysAgoCalendar().getTime();
	}
	
	public static Calendar sevenDaysAgoCalendar() {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		date.add(Calendar.DAY_OF_MONTH, -7);

		return date;

	}

	public static Date thirtyDaysAgo() {
		return thirtyDaysAgoCalendar().getTime();
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
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Date now() {
		return new Date();
	}

	public static Set<String> extractVariables(String str) {
		Set<String> vars = new HashSet<>();
		int idx = 0;
		while ((idx = str.indexOf('{', idx)) != -1) {
			int start = idx + 1;
			idx = str.indexOf('}', start);
			if (idx == -1) {
				throw new IllegalArgumentException("Unterminated variable detected!");
			}
			vars.add(str.substring(start, idx));
			idx++;
		}
		return vars;
	}

	public static String getCommaSeparatedNames(Collection<? extends NamedDocument> values) {

		StringBuffer buf = new StringBuffer();
		for (NamedDocument value : values) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(value.getName());
		}

		return buf.toString();
	}

	public static Date getMonthEnd(Date timestamp) {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.setTime(getMonthStart(timestamp));
		date.add(Calendar.MONTH, 1);
		date.add(Calendar.DAY_OF_MONTH, -1);
		date.set(Calendar.HOUR_OF_DAY, 23);
		date.set(Calendar.MINUTE, 59);
		date.set(Calendar.SECOND, 59);
		date.set(Calendar.MILLISECOND, 9999);
		return date.getTime();

	}

	public static Date getMonthStart(Date timestamp) {

		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.setTime(timestamp);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime();
	}

	public static Long getLongOrDefault(String value, Long defaultValue) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
		}
		return defaultValue;
	}

	public static String stripNonAlphanumeric(String str) {
		return str = str.replaceAll("[^a-zA-Z0-9]", "");
	}

	public static boolean isNotCompanyType(String element) {

		element = stripNonAlphanumeric(element);

		switch (element.trim().toLowerCase()) {
		case "ltd":
		case "inc":
		case "limited":
		case "plc":
		case "gmbh":
		case "ag":
		case "llc":
		case "private":
			return false;
		default:
			return true;
		}
	}

	public static String generate3LetterCode(String name, char fillingCharacter) {
		return generateUsername(name, fillingCharacter, 3);
	}

	public static String generateUsername(String name, char fillingCharacter, int length) {
		name = WordUtils.capitalizeFully(stripNonAlphanumeric(name));
		String[] elements = name.split(" ");
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			if (i < elements.length && StringUtils.isNotBlank(elements[i])) {
				if (Utils.isNotCompanyType(elements[i])) {
					buf.append(elements[i].charAt(0));
				}
			} else {
				buf.append(fillingCharacter);
			}
		}
		return buf.toString();
	}

	public static String intitals(String name) {
		name = WordUtils.capitalizeFully(name);
		String[] elements = name.split(" ");
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			buf.append(elements[i].charAt(0));
		}
		return buf.toString();
	}

	public static Double round(Double value) {
		return round(value, 2);
	}

	public static Double round(Double value, int digits) {
		return BigDecimal.valueOf(value).setScale(digits, RoundingMode.HALF_DOWN).doubleValue();
	}

	public static String generateShortName(String val, int i) {
		StringBuffer buf = new StringBuffer();
		String str = "abcdefghijklmnopqrstuvwxyz";
		val = val.toLowerCase();
		for (Character c : val.toCharArray()) {

			if (str.indexOf(c) > -1) {
				buf.append(c);
			}
			if (buf.length() >= i) {
				break;
			}

		}
		return buf.toString();
	}

	public static Long fromByteSize(String val) {
		if (val.matches("\\d+")) {
			return Long.parseLong(val);
		}

		Pattern p = Pattern.compile("(\\d+)(.*)");
		Matcher m = p.matcher(val);
		if (!m.matches()) {
			throw new IllegalArgumentException(String.format("Invalid input %s", val));
		}
		String n = m.group(1);
		String t = m.group(2);

		t = t.toUpperCase();

		Long v = Long.parseLong(n);

		switch (t) {
		case "P":
		case "PB":
			return v * 1000 * 1000 * 1000 * 1000 * 1000;
		case "PIB":
			return v * 1024 * 1024 * 1024 * 1024 * 1024;
		case "T":
		case "TB":
			return v * 1000 * 1000 * 1000 * 1000;
		case "TIB":
			return v * 1024 * 1024 * 1024 * 1024;
		case "G":
		case "GB":
			return v * 1000 * 1000 * 1000;
		case "GIB":
			return v * 1024 * 1024 * 1024;
		case "M":
		case "MB":
			return v * 1000 * 1000;
		case "MIB":
			return v * 1024 * 1024;
		case "K":
		case "KB":
			return v * 1000;
		case "KIB":
			return v * 1024;
		default:
			throw new IllegalArgumentException(String.format("Invalid input %s", val));
		}
	}

	public static String toByteSize(double t) {
		return toByteSize(t, 2);
	}

	public static String toByteSize(double t, int decimalPlaces) {

		if (decimalPlaces < 0) {
			throw new IllegalArgumentException("Number of decimal places must be > 0");
		}
		String[] sizes = { "B", "KB", "MB", "GB", "TB", "PB" };
		int idx = 0;
		double x = t;
		while (x / 1000 >= 1) {
			idx++;
			x = (x / 1000);
		}

		return String.format("%." + decimalPlaces + "f%s", x, sizes[idx]);
	}

	/*
	 * GNU LESSER GENERAL PUBLIC LICENSE Copyright (C) 2006 The XAMJ Project
	 * 
	 * This library is free software; you can redistribute it and/or modify it under
	 * the terms of the GNU Lesser General Public License as published by the Free
	 * Software Foundation; either version 2.1 of the License, or (at your option)
	 * any later version.
	 * 
	 * This library is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
	 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
	 * details.
	 * 
	 * You should have received a copy of the GNU Lesser General Public License
	 * along with this library; if not, write to the Free Software Foundation, Inc.,
	 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
	 * 
	 * Contact info: lobochief@users.sourceforge.net
	 */
	public static String textToHTML(String text) {
		if (text == null) {
			return null;
		}
		int length = text.length();
		boolean prevSlashR = false;
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
			case '\r':
				if (prevSlashR) {
					out.append("<br>");
				}
				prevSlashR = true;
				break;
			case '\n':
				prevSlashR = false;
				out.append("<br>");
				break;
			case '"':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&quot;");
				break;
			case '<':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&lt;");
				break;
			case '>':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&gt;");
				break;
			case '&':
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append("&amp;");
				break;
			default:
				if (prevSlashR) {
					out.append("<br>");
					prevSlashR = false;
				}
				out.append(ch);
				break;
			}
		}
		return out.toString();
	}

	public static String formatCurrency(Number value) {
		return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value);
	}

	public static String urlToBase64Image(String pictureUrl) throws IOException {
		URL url = URI.create(pictureUrl).toURL();
		URLConnection c = url.openConnection();
		return urlToBase64Image(c.getContentType(), c.getInputStream());
	}
	
	public static String urlToBase64Image(String contentType, InputStream source) throws IOException {
		return String.format("data:%s;base64, %s", 
				contentType,
				Utils.base64Encode(IOUtils.toByteArray(source)));
	}
	
	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
	    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
	    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
	    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
	    return outputImage;
	}
	
	/**
     * Loads a BufferedImage from a Base64 encoded string typically found in an HTML img src attribute.
     *
     * @param base64EncodedImage The full base64 string from the img src, e.g., "data:image/png;base64,..."
     * @return A BufferedImage object, or null if decoding fails.
     * @throws IOException If an error occurs during image reading.
     */
    public static BufferedImage loadBufferedImageFromBase64Src(String base64EncodedImage) throws IOException {
        if (base64EncodedImage == null || base64EncodedImage.isEmpty()) {
            return null;
        }

        // 1. Extract the Base64 String by removing the prefix
        // The prefix usually looks like "data:image/png;base64,"
        // or "data:image/jpeg;base64," etc.
        String base64String = base64EncodedImage;
        int commaIndex = base64EncodedImage.indexOf(',');
        if (commaIndex != -1) {
            base64String = base64EncodedImage.substring(commaIndex + 1).trim();
        } else {
            // Handle cases where the prefix might be missing, though it's less common for img src
            // You might want to throw an IllegalArgumentException here if the prefix is mandatory for your use case.
            System.err.println("Warning: Base64 string does not contain a data URL prefix. Attempting to decode directly.");
        }

        // 2. Decode the Base64 String into a byte array
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string: " + e.getMessage());
            return null; // Or rethrow as a more specific exception
        }

        // 3. Read the bytes into a BufferedImage
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                System.err.println("Error: Could not read image from byte array. Invalid image format or corrupted data.");
            }
            return image;
        }
    }
    
    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw); // Print the stack trace to the PrintWriter
        return sw.toString(); // Return the accumulated string
    }

	public static Date startOfDay(Date effectiveDate) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(effectiveDate);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	public static Date nextDay(Date effectiveDate) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(DateUtils.addDays(effectiveDate, 1));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	public static Date trim(Date date) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	public static void readTarGZ(String filePath) throws Exception {

        try (InputStream fileStream = new FileInputStream(filePath);

             InputStream bufferedStream = new BufferedInputStream(fileStream);
             GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(bufferedStream);

             TarArchiveInputStream tarStream = new TarArchiveInputStream(gzipStream)) {

            TarArchiveEntry entry;
            // Loop through each file/directory entry inside the TAR archive
            while ((entry = tarStream.getNextEntry()) != null) {
                
                String entryName = entry.getName();
                
                if (entry.isDirectory()) {
                    System.out.println("Directory found: " + entryName);
                    continue;
                }
                
                System.out.println("File found: " + entryName);
                
                // Read the contents of the file entry
                // Note: The content reading is streamlined by the TarArchiveInputStream
                int bytesRead;
                byte[] buffer = new byte[4096];
                
                // Read content bytes from tarStream until the end of the current entry
                while ((bytesRead = tarStream.read(buffer, 0, buffer.length)) != -1) {
                    // Process or save the file content here
                    // e.g., print it, save it to a new file, or feed it to your AI processor
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
