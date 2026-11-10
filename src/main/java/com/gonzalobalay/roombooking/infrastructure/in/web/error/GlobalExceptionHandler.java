package com.gonzalobalay.roombooking.infrastructure.in.web.error;

import com.gonzalobalay.roombooking.domain.exception.*;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.FieldErrorDetail;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RoomNotFoundException.class)
    public ProblemDetail handleRoomNotFoundException(RoomNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("Room not found");
        return problemDetail;
    }

    @ExceptionHandler(RoomCapacityExceededException.class)
    public ProblemDetail handleRoomCapacityExceeded(RoomCapacityExceededException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problemDetail.setTitle("Room capacity exceeded");
        return problemDetail;
    }

    @ExceptionHandler(BookingOverlapException.class)
    public ProblemDetail handleBookingOverlapException(BookingOverlapException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setTitle("Booking overlap");
        return problemDetail;
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFoundException(BookingNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setTitle("Booking not found");
        return problemDetail;
    }


    @ExceptionHandler(RoomConcurrentModificationException.class)
    public ProblemDetail handleRoomConcurrentModificationException(RoomConcurrentModificationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setTitle("Room has been modified");
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ex.getBody();
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(status).headers(headers).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ex.getBody();
        List<FieldErrorDetail> errors = ex.getParameterValidationResults().stream()
                .map(error -> new FieldErrorDetail(error.getMethodParameter().getParameterName(), error.getResolvableErrors().getFirst().getDefaultMessage()))
                .toList();
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(status).headers(headers).body(problemDetail);
    }
}
