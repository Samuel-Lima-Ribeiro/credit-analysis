package com.client.credit.analysis.exceptionhandler;

import com.client.credit.analysis.exception.ClientNotFoundException;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControlExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ProblemDetail clientNotFoundHandleException(ClientNotFoundException exception) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problemDetail.setType(URI.create("https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Status/422"));
        problemDetail.setProperty("Timestamp", LocalDateTime.now());
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
