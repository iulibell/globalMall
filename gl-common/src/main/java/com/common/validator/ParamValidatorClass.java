package com.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ParamValidatorClass implements ConstraintValidator<ParamValidator, String> {
    private String[] values;

    @Override
    public void initialize(ParamValidator paramValidator) {
        this.values = paramValidator.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (String allowed : values) {
            if (allowed.equals(value)) {
                return true;
            }
        }
        return false;
    }
}