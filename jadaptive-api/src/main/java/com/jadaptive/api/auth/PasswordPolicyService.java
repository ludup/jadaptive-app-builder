package com.jadaptive.api.auth;

import java.util.Collection;
import java.util.List;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.user.User;

public interface PasswordPolicyService extends AbstractUUIDObjectService<PasswordPolicy> {

    final class PasswordValidationResult {

        private final boolean valid;
        private final List<String> errors;

        private PasswordValidationResult(boolean valid, Collection<String> errors) {
            this.valid = valid;
            this.errors = List.copyOf(errors);
        }

        public static PasswordValidationResult valid() {
            return new PasswordValidationResult(true, List.of());
        }

        public static PasswordValidationResult invalid(Collection<String> errors) {
            return new PasswordValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

	void verifyPassword(User user, char[] password);

	PasswordValidationResult validatePassword(char[] password, PasswordPolicy policy);
}
