package com.socialnetwork.boardrift.util;

import com.socialnetwork.boardrift.util.exception.DuplicateFriendRequestException;
import com.socialnetwork.boardrift.util.exception.DuplicatePollVoteException;
import com.socialnetwork.boardrift.util.exception.DuplicateReportException;
import com.socialnetwork.boardrift.util.exception.EmailNotVerifiedException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenExpiredException;
import com.socialnetwork.boardrift.util.exception.EmailVerificationTokenNotFoundException;
import com.socialnetwork.boardrift.util.exception.InvalidLoginCredentialsException;
import com.socialnetwork.boardrift.util.exception.RefreshTokenNotFoundException;
import com.socialnetwork.boardrift.util.exception.TokenRefreshException;
import com.socialnetwork.boardrift.util.exception.TooManyWarningsException;
import com.socialnetwork.boardrift.util.exception.UnauthorizedException;
import com.socialnetwork.boardrift.util.exception.UserAlreadySuspendedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler(DuplicatePollVoteException.class)
    public ResponseEntity<String> handleDuplicatePollVoteException(DuplicatePollVoteException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TooManyWarningsException.class)
    public ResponseEntity<String> handleTooManyWarningsException(TooManyWarningsException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<String> handleDuplicateReportException(DuplicateReportException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadySuspendedException.class)
    public ResponseEntity<String> handleUserAlreadySuspendedException(UserAlreadySuspendedException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

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

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<String> handleInvalidLoginCredentialsException(InvalidLoginCredentialsException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<String> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<String> handleTokenRefreshException(TokenRefreshException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> handleEmailNotVerifiedException(EmailNotVerifiedException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateFriendRequestException.class)
    public ResponseEntity<String> handleDuplicateFriendRequestException(DuplicateFriendRequestException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleIllegalAccessException(IllegalAccessException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException exception, HttpServletRequest request) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
