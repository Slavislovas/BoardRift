package com.socialnetwork.boardrift.util.exception;

public class UserAlreadySuspendedException extends RuntimeException{
    public UserAlreadySuspendedException() {
        super("This user is already suspended");
    }
}
