package com.socialnetwork.boardrift.util;

import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestControllerExceptionHandler {
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<Map<String, String>> handleFieldValidationException(FieldValidationException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getFieldErrors(), HttpStatus.BAD_REQUEST);
    }
}
