package com.jadaptive.api.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PasswordPolicyServiceImplTest {

    private final PasswordPolicyServiceImpl service = new PasswordPolicyServiceImpl();

    @Test
    void validPasswordPasses() {
        PasswordPolicy policy = buildPolicy(10, 1, 1, 1, 1, "!@#$");

        PasswordPolicyService.PasswordValidationResult result = service.validatePassword("Abcdefgh1!".toCharArray(), policy);

        assertTrue(result.isValid(), "Expected password to comply with policy");
        assertTrue(result.getErrors().isEmpty(), "Expected no validation errors");
    }

    @Test
    void shortAndMissingCharacterClassesFails() {
        PasswordPolicy policy = buildPolicy(10, 1, 1, 1, 1, "!@#$");

        PasswordPolicyService.PasswordValidationResult result = service.validatePassword("abc".toCharArray(), policy);

        assertFalse(result.isValid(), "Expected password to fail policy");
        List<String> expected = List.of(
                "Password must be at least 10 characters long.",
                "Password must contain at least 1 uppercase character(s).",
                "Password must contain at least 1 numeric character(s).",
                "Password must contain at least 1 special character(s)."
        );
        assertIterableEquals(expected, result.getErrors());
    }

    @Test
    void disallowedSpecialCharacterFails() {
        PasswordPolicy policy = buildPolicy(8, 1, 1, 1, 1, "!");

        PasswordPolicyService.PasswordValidationResult result = service.validatePassword("Abcdefg1#".toCharArray(), policy);

        assertFalse(result.isValid(), "Expected password to fail policy due to disallowed character");
        assertEquals(2, result.getErrors().size(), "Expected disallowed character and missing special errors");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Character '#'") ), "Expected disallowed character message");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("special character")), "Expected special character requirement message");
    }

    private PasswordPolicy buildPolicy(int minLength, int minLower, int minUpper, int minNumeric, int minSpecial, String allowedSpecials) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setMinimumLength(minLength);
        policy.setMinimumLowercase(minLower);
        policy.setMinimumUppercase(minUpper);
        policy.setMinimumNumeric(minNumeric);
        policy.setMinimumSpecial(minSpecial);
        policy.setAllowedSpecialCharacters(allowedSpecials);
        return policy;
    }
}
