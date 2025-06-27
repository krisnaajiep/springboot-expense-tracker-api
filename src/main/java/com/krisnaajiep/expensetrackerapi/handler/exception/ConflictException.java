package com.krisnaajiep.expensetrackerapi.handler.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
