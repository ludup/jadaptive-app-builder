package com.jadaptive.api.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.AbstractAssignableUUIDObjectServiceImpl;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@Service
public class PasswordPolicyServiceImpl extends AbstractAssignableUUIDObjectServiceImpl<PasswordPolicy> implements PasswordPolicyService {

    @Override
    protected Class<PasswordPolicy> getResourceClass() {
        return PasswordPolicy.class;
    }

    @Override
    public PasswordPolicy createNew(ObjectTemplate template) {
        return new PasswordPolicy();
    }

    @Override
    public PasswordValidationResult validatePassword(char[] password, PasswordPolicy policy) {

        List<String> errors = new ArrayList<>();

        if (Objects.isNull(policy)) {
            errors.add("Password policy is required.");
            return PasswordValidationResult.invalid(errors);
        }

        if (password == null || password.length == 0) {
            errors.add("Password must not be empty.");
            return PasswordValidationResult.invalid(errors);
        }

        String allowedSpecials = Objects.toString(policy.getAllowedSpecialCharacters(), "");

        int minimumLength = defaultValue(policy.getMinimumLength());
        int minimumLower = defaultValue(policy.getMinimumLowercase());
        int minimumUpper = defaultValue(policy.getMinimumUppercase());
        int minimumNumeric = defaultValue(policy.getMinimumNumeric());
        int minimumSpecial = defaultValue(policy.getMinimumSpecial());

        if (password.length < minimumLength) {
            errors.add(String.format("Password must be at least %d characters long.", minimumLength));
        }

        int lower = 0;
        int upper = 0;
        int numeric = 0;
        int special = 0;

        for (char c : password) {
            if (Character.isLowerCase(c)) {
                lower++;
            } else if (Character.isUpperCase(c)) {
                upper++;
            } else if (Character.isDigit(c)) {
                numeric++;
            } else {
                if (allowedSpecials.indexOf(c) > -1) {
                    special++;
                } else {
                    errors.add(String.format("Character '%s' is not allowed.", c));
                }
            }
        }

        if (lower < minimumLower) {
            errors.add(String.format("Password must contain at least %d lowercase character(s).", minimumLower));
        }

        if (upper < minimumUpper) {
            errors.add(String.format("Password must contain at least %d uppercase character(s).", minimumUpper));
        }

        if (numeric < minimumNumeric) {
            errors.add(String.format("Password must contain at least %d numeric character(s).", minimumNumeric));
        }

        if (special < minimumSpecial) {
            errors.add(String.format("Password must contain at least %d special character(s).", minimumSpecial));
        }

        return errors.isEmpty() ? PasswordValidationResult.valid() : PasswordValidationResult.invalid(errors);
    }

    private int defaultValue(Integer value) {
        return Math.max(0, Objects.nonNull(value) ? value.intValue() : 0);
    }

	@Override
	public void verifyPassword(User user, char[] password) {
		
		try {
			PasswordPolicy policy = getEffectivePasswordPolicy(user);
			PasswordValidationResult result = validatePassword(password, policy);
			if(result.isValid()) {
				return;
			} else {
				throw new ObjectException(PasswordPolicy.RESOURCE_KEY, "passwordPolicy.error",  Utils.csv(result.getErrors()));
			}
		} catch(ObjectNotFoundException e) {
			// No policy found, so we assume the password is valid
		}
	}

	private PasswordPolicy getEffectivePasswordPolicy(User user) {
		PasswordPolicy effectivePolicy = null;
		for(PasswordPolicy policy : objectDatabase.getAssignedObjectsA(PasswordPolicy.class, user)) {
			if(effectivePolicy == null || isMoreRestrictive(policy, effectivePolicy)) {
				effectivePolicy = policy;
			}
		}
		if(effectivePolicy == null) {
			throw new ObjectNotFoundException("No password policy assigned to user");
		}
		return effectivePolicy;
	}

	private boolean isMoreRestrictive(PasswordPolicy policy, PasswordPolicy effectivePolicy) {
		return policy.getMinimumLength() > effectivePolicy.getMinimumLength() ||
				policy.getMinimumLowercase() > effectivePolicy.getMinimumLowercase() ||
				policy.getMinimumUppercase() > effectivePolicy.getMinimumUppercase() ||
				policy.getMinimumNumeric() > effectivePolicy.getMinimumNumeric() ||
				policy.getMinimumSpecial() > effectivePolicy.getMinimumSpecial();
	}

	@Override
	public PasswordPolicy getDefaultPolicy() {
		return getObjectByUUID(PasswordPolicyTenantInitializer.DEFAULT_POLICY_UUID);
	}
}