package com.jadaptive.app.db;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.app.I18N;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationException;
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
		case PERMISSION:
			validatePermission(value, field);
			return value;
		case TEXT:
		case TEXT_AREA:
		case PASSWORD:
			validateText(value, field);
			return value;
		case OBJECT_REFERENCE:
//			if(node.isObject()) {
//				return validateReference(node, field);
//			}
			return value;
		default:
			throw new ValidationException(
					String.format("Missing field type %s in validate method", field.getFieldType().name()));
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
				case REGEX:
				{
					validateRegex(v.getValue(), value, v, field);
					break;
				}
				case URL:
				{
					if(StringUtils.isNotBlank(value)) {
						validateRegex(Utils.HTTP_URL_PATTERN, value, v, field);
					}
					break;
				}
				default:
					break;
				}
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
