package com.socialnetwork.boardrift.util.exception;

public class EmailVerificationTokenExpiredException extends RuntimeException {
    public EmailVerificationTokenExpiredException() {
        super("Email verification token has expired");
    }
}
