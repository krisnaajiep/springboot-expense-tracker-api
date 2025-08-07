package com.krisnaajiep.expensetrackerapi.handler.exception;

import lombok.Getter;
import org.springframework.http.HttpHeaders;

@Getter
public class TooManyRequestsException extends RuntimeException {
    private final HttpHeaders headers = new HttpHeaders();

    public TooManyRequestsException(String message, Long retryAfter) {
        super(message);
        headers.add("Retry-After", retryAfter.toString());
    }
}
