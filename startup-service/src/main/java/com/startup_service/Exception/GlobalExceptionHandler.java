package com.startup_service.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(LocalDateTime.now(), ex.getStatusCode().value(), ex.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(403).body(new ErrorResponse(LocalDateTime.now(), 403, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(400).body(new ErrorResponse(LocalDateTime.now(), 400, msg, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(500).body(new ErrorResponse(LocalDateTime.now(), 500, "Internal server error: " + ex.getMessage(), request.getRequestURI()));
    }
}
