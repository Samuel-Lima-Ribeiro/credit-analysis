package com.client.credit.analysis.logger;

import org.springframework.util.StringUtils;

public class EnvUtil {

    public static String getEnvOrError(String varName) {
        final String value = System.getenv(varName);
        if (StringUtils.hasText(value)) {
            throw new IllegalStateException("Environment variable(" + varName + ") is not provided");
        } else {
            return value;
        }
    }

    public static String getEnvOrDefault(String varName, String defaultValue) {
        final String value = System.getenv(varName);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
