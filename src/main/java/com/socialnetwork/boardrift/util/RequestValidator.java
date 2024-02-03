package com.socialnetwork.boardrift.util;

import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

public class RequestValidator {
    public static void validateRequest(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            throw new FieldValidationException(fieldErrors);
        }
    }
}
