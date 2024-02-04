package com.socialnetwork.boardrift.util.exception;

public class InvalidLoginCredentialsException extends RuntimeException{
    public InvalidLoginCredentialsException() {
        super("Invalid username or password");
    }
}
