package org.example.library.common.localization;

import java.util.Objects;

public record DefaultLanguage(String code) {

    public DefaultLanguage {
        Objects.requireNonNull(code, "Default language code must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("Default language code must not be blank");
        }
    }

}
