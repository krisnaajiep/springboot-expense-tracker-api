package com.krisnaajiep.expensetrackerapi.handler.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
