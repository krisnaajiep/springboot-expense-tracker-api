package com.krisnaajiep.expensetrackerapi.handler;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 22.57
@Last Modified 27/06/25 22.57
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.handler.exception.UnauthorizedException;
import com.krisnaajiep.expensetrackerapi.util.ValidationUtility;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        BindingResult result = ex.getBindingResult();
        Optional<Object> target = Optional.ofNullable(result.getTarget());
        Class<?> dtoClass = target.map(Object::getClass).orElse(null);
        result.getFieldErrors().forEach(error -> {
                    String fieldName = dtoClass != null
                            ? ValidationUtility.getJsonPropertyName(error, dtoClass)
                            : error.getField();

                    errors.put(fieldName, error.getDefaultMessage());
                }
        );

        return new ResponseEntity<>(Map.of("errors", errors), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NotNull HttpMessageNotReadableException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request
    ) {
        logger.error("An error occurred while parsing the request body: " + ex.getMessage(), ex);
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        logger.error("An unexpected error occurred: " + ex.getMessage(), ex);
        return new ResponseEntity<>(
                Map.of("message", "Internal server error"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Invalid argument: " + ex.getMessage(), ex);
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleDuplicateKey(ConflictException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Invalid credentials: " + ex.getMessage());
        return new ResponseEntity<>(Map.of("message", "Invalid credentials"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: " + ex.getMessage());
        return new ResponseEntity<>(Map.of("message", "Forbidden"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
