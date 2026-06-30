package com.example.devopsai.common.security;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public final class SensitiveDataMasker {

    private static final String MASK = "******";
    private static final Set<String> SENSITIVE_NAMES = Set.of(
            "authorization",
            "token",
            "access_token",
            "refresh_token",
            "password",
            "password_hash",
            "api_key",
            "apikey",
            "secret",
            "jwt_secret"
    );
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "(?i)(authorization|token|access_token|refresh_token|password|password_hash|api[_-]?key|secret|jwt_secret)" +
                    "\\s*[:=]\\s*([^\\s,;&}]+)"
    );

    private SensitiveDataMasker() {
    }

    public static String maskByName(String name, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return isSensitiveName(name) ? MASK : maskInline(value);
    }

    public static String maskInline(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return KEY_VALUE_PATTERN.matcher(value).replaceAll("$1=" + MASK);
    }

    public static boolean isSensitiveName(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        var normalized = name.trim().toLowerCase(Locale.ROOT).replace("-", "_");
        return SENSITIVE_NAMES.contains(normalized);
    }
}
