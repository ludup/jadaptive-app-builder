package com.jadaptive.app.db;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.utils.Utils;

public class DocumentValidator {

	public static Object validate(FieldTemplate field, String value)
			throws ValidationException {

		switch (field.getFieldType()) {
		case BOOL:
			validateBoolean(value, field);
			return Boolean.valueOf(value);
		case DECIMAL:
			validateDecimal(value, field);
			return Double.valueOf(value);
		case LONG:
			validateNumber(value, field);
			return Long.valueOf(value);
		case INTEGER:
			validateNumber(value, field);
			return Integer.valueOf(value);
		case TIMESTAMP:
			validateDate(value, field);
			return Utils.parseDate(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		case DATE:
			validateDate(value, field);
			return Utils.parseDate(value, "yyyy-MM-dd");
		case ENUM:
			validateEnum(value, field);
			return value;
		case TIME:
			
			return value;
		case PERMISSION:
			validatePermission(value, field);
			return value;
		case TEXT:
		case TEXT_AREA:
		case PASSWORD:
			validateText(value, field);
			return value;
		case OBJECT_REFERENCE:
			return validateReference(value, field);
		default:
			throw new ValidationException(
					String.format("Missing field type %s in validate method", field.getFieldType().name()));
		}

	}

	private static Object validateReference(String value, FieldTemplate field) {
		
		String resourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
		
		try {
			ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(
					ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(resourceKey), value);
			return value;
		} catch(ObjectNotFoundException e) {
			throw new IllegalStateException("Invalid reference to " + field.getResourceKey());
		}
	}

	private static void validatePermission(String value, FieldTemplate field) throws ValidationException {

		PermissionService service = ApplicationServiceImpl.getInstance().getBean(PermissionService.class);
		if (!service.isValidPermission(value)) {
			throw new ValidationException(String.format("%s is not a valid permission", value));
		}
	}

	private static void validateDate(String value, FieldTemplate field) {

	}

	private static void validateEnum(String value, FieldTemplate field) {

	}

	private static void validateNumber(String val, FieldTemplate field) throws ValidationException {
		try {
			long value = Long.parseLong(val);

			if (!Objects.isNull(field.getValidators())) {
				for (FieldValidator v : field.getValidators()) {
					switch (v.getType()) {
					case RANGE:
						String[] range = v.getValue().split("-");
						if (range.length != 2) {
							throw new ValidationException(String.format(
									"Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						try {
							long min = Long.parseLong(range[0]);
							long max = Long.parseLong(range[1]);
							if (value < min || value > max) {
								throw new ValidationException(String.format("%s must be in the range %d to %d",
										field.getResourceKey(), min, max));
							}
						} catch (NumberFormatException e) {
							throw new ValidationException(String.format(
									"Invalid range %s value in validator use \"<min>,<max>\" format", v.getValue()));
						}
						break;
					case PORT:
					{
						if(value > 0 && value < 65536) {
							return;
						}
						throw new ValidationException("Invalid port number! Port value must be within the range 1-65535");
					}
					default:
						break;
					}
				}
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(
					String.format("Value %s for field %s is not a number", val, field.getResourceKey()));
		}
	}

	private static void validateDecimal(String val, FieldTemplate field) throws ValidationException {
		try {
			double value = Double.parseDouble(val);

			if (!Objects.isNull(field.getValidators())) {
				for (FieldValidator v : field.getValidators()) {
					switch (v.getType()) {
					case RANGE:
						String[] range = v.getValue().split("-");
						if (range.length != 2) {
							throw new ValidationException(String.format(
									"Invalid range %s value in validator use \"<min>-<max>\" format", v.getValue()));
						}
						try {
							double min = Double.parseDouble(range[0]);
							double max = Double.parseDouble(range[1]);
							if (value < min || value > max) {
								throw new ValidationException(String.format("%s must be in the range %d to %d",
										field.getResourceKey(), min, max));
							}
						} catch (NumberFormatException e) {
							throw new ValidationException(String.format(
									"Invalid range %s value in validator use \"<min>-<max>\" format", v.getValue()));
						}
						break;
					default:
						break;
					}
				}
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(
					String.format("Value %s for field %s is not a double", val, field.getResourceKey()));
		}
	}

	private static void validateBoolean(String value, FieldTemplate field) throws ValidationException {

		switch (value) {
		case "true":
		case "TRUE":
		case "false":
		case "FALSE":
			return;
		default:
			throw new ValidationException(
					String.format("Value %s for field %s is not a boolean", value, field.getResourceKey()));
		}
	}

	private static void validateText(String value, FieldTemplate field) throws ValidationException {

		if (!Objects.isNull(field.getValidators())) {
			for (FieldValidator v : field.getValidators()) {
				switch (v.getType()) {
				case REQUIRED:
				{
					if(StringUtils.isBlank(value)) {
						if(StringUtils.isBlank(v.getI18n())) {
							throw new ValidationException(
									I18N.getResource(Locale.getDefault(), "default", "default.required.error", 
										I18N.getResource(Locale.getDefault(), v.getBundle(), String.format("%s.name", field.getResourceKey()))));
						} else {
							throw new ValidationException(I18N.getResource(
								Locale.getDefault(), v.getBundle(), v.getI18n(), value));
						}
					}
					break;
				}
				case LENGTH:
				{
					int maxlength = Integer.parseInt(v.getValue());
					if (value.length() > maxlength) {
						throw new ValidationException(
								String.format("%s must be less than %d characters", field.getResourceKey(), maxlength));
					}
					break;
				}
				case URL:
				{
					if(StringUtils.isNotBlank(value)) {
						validateRegex(Utils.HTTP_URL_PATTERN, value, v, field);
					}
					break;
				}
				case EMPTY:
				{
					if(StringUtils.isBlank(value)) {
						return;
					}
					break;
				}
				default:
					break;
				}
			}
			
			/**
			 * We allow multiple REGEX validations, If one succeeds, validation succeeds. Otherwise
			 * we throw the last validation error we received.
			 */
			ValidationException lastValidationError = null;
			for (FieldValidator v : field.getValidators()) {
				try {
				switch (v.getType()) {
				case IPV6:
				{
					validateRegex("^((([0-9A-Fa-f]{1,4}:){1,6}:)|(([0-9A-Fa-f]{1,4}:){7}))([0-9A-Fa-f]{1,4})$", 
							value, v, field);
					return;
				}
				case IPV4:
				{
					validateRegex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", 
							value, v, field);
					return;
				}
				case CIDR_V6:
				{
					validateRegex("^(?:(?:(?:[A-F0-9]{1,4}:){6}|(?=(?:[A-F0-9]{0,4}:){0,6}(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?![:.\\w]))(([0-9A-F]{1,4}:){0,5}|:)((:[0-9A-F]{1,4}){1,5}:|:)|::(?:[A-F0-9]{1,4}:){5})(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(?:[A-F0-9]{1,4}:){7}[A-F0-9]{1,4}|(?=(?:[A-F0-9]{0,4}:){0,7}[A-F0-9]{0,4}(?![:.\\w]))(([0-9A-F]{1,4}:){1,7}|:)((:[0-9A-F]{1,4}){1,7}|:)|(?:[A-F0-9]{1,4}:){7}:|:(:[A-F0-9]{1,4}){7})(?![:.\\w])\\/(?:12[0-8]|1[01][0-9]|[1-9]?[0-9])$", 
							value, v, field);
					return;
				}
				case CIDR_V4:
				{
					validateRegex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(/(3[0-2]|2[0-9]|1[0-9]|[0-9]))?$", 
							value, v, field);
					return;
				}
				case HOSTNAME:
				{
					validateRegex("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$", 
							value, v, field);
					return;
				}
				case REGEX:
				{
					validateRegex(v.getValue(), value, v, field);
					return;
				}
				default:
					break;
				}
				
				} catch(ValidationException e) {
					lastValidationError = e;
				}
			}
			
			if(Objects.nonNull(lastValidationError)) {
				throw lastValidationError;
			}
			
		}
	}
	
	private static void validateRegex(String regex, String value, FieldValidator v, FieldTemplate field) {
		
		if(StringUtils.isNotBlank(value)) {
			Pattern pattern = Pattern.compile(regex);
			if (!pattern.matcher(value).matches()) {
				if(StringUtils.isBlank(v.getI18n())) {
					throw new ValidationException(String.format("%s is an invalid value for %s", value,
							I18N.getResource(Locale.getDefault(), v.getBundle(), field.getResourceKey() + ".name")));
				} else {
					throw new ValidationException(I18N.getResource(
						Locale.getDefault(), v.getBundle(), v.getI18n(), value));
				}
			}
		}
	}
}
