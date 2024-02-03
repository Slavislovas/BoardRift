package com.socialnetwork.boardrift.util;

import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
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

    @ExceptionHandler(EmailVerificationTokenNotFoundException.class)
    public ResponseEntity<String> handleEmailVerificationTokenNotFoundException(EmailVerificationTokenNotFoundException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailVerificationTokenExpiredException.class)
    public ResponseEntity<String> handleEmailVerificationTokenExpiredException(EmailVerificationTokenExpiredException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }
}
