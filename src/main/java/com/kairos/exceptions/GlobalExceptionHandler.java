package com.kairos.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros de validação (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", message,
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.badRequest().body(error);
    }

    // Argumento inválido (ex: email duplicado, membro já existe etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage(),
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.badRequest().body(error);
    }

    // Erro de parsing JSON (ex: data inválida como 2026-09-31)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String message = "Dados inválidos no corpo da requisição.";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            if (ife.getCause() instanceof DateTimeParseException) {
                message = "Data inválida: '" + ife.getValue() + "'. Use o formato YYYY-MM-DD com uma data real (ex: setembro tem apenas 30 dias).";
            }
        }
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", message,
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.badRequest().body(error);
    }

    // Acesso negado (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(), "FORBIDDEN",
                "Você não tem permissão para realizar esta ação.",
                request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // Fallback genérico
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiError> handleAllExceptions(Exception ex, WebRequest request) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
