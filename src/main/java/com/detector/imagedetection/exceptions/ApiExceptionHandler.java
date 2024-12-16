package com.detector.imagedetection.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.detector.imagedetection.exceptions.model.BadRequestException;
import com.detector.imagedetection.exceptions.model.ErrorResponse;
import com.detector.imagedetection.exceptions.model.InternalServerErrorException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(value = {BadRequestException.class})
    protected ResponseEntity<Object> handleBadRequestException(
        RuntimeException ex, WebRequest request) {
            ErrorResponse error = new ErrorResponse("Bad Request", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

    @ExceptionHandler(value = {InternalServerErrorException.class, InternalServerError.class})
    protected ResponseEntity<Object> handleInternalServerException(
        RuntimeException ex, WebRequest request) {;
            ErrorResponse error = new ErrorResponse("Internal Server Error", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
