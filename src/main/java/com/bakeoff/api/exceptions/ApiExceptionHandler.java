package com.bakeoff.api.exceptions;

import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
      SQLIntegrityConstraintViolationException.class,
      InvalidFormatException.class
  })
  public ExceptionResponse invalidFormatException(Exception ex, HttpServletRequest httpRequest) {
    log.error(ex.getClass().getSimpleName() + " exception: ", ex);
    return new ExceptionResponse(ErrorCodes.INVALID_FORMAT.name(),
        ex.getMessage());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({
      NotFoundException.class
  })
  public ExceptionResponse notFoundException(Exception ex, HttpServletRequest httpRequest) {
    log.error(ex.getClass().getSimpleName() + " exception: ", ex);
    return new ExceptionResponse(ErrorCodes.NOT_FOUND.name(),
        ex.getMessage());
  }

  private enum ErrorCodes {
    INVALID_FORMAT,
    NOT_FOUND,
    INTERNAL_EXCEPTION,
    CONFLICT,
    UNPROCESSABLE_ENTITY,
    METHOD_NOT_ALLOWED
  }

  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExceptionResponse {

    @Getter
    private String errorCode;

    @Getter
    private String errorMessage;
  }
}