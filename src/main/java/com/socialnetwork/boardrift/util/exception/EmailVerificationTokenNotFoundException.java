package com.socialnetwork.boardrift.util.exception;

public class EmailVerificationTokenNotFoundException extends RuntimeException {
    public EmailVerificationTokenNotFoundException() {
        super("Email verification token not found");
    }
}
