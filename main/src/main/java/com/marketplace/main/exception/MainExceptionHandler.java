package com.marketplace.main.exception;

import com.marketplace.common.exception.CommonExceptionService;
import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.common.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.stream.Collectors;

import static com.marketplace.common.constants.Delimiters.COMMA_DELIMITER;
import static com.marketplace.common.constants.Delimiters.COLON_DELIMITER;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class MainExceptionHandler {

    private final CommonExceptionService commonExceptionService;

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request, HttpServletResponse response) {

        String invalidFields = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(COMMA_DELIMITER));

        response.setStatus(400);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                    .modelView("error")
                    .status(400)
                    .message(invalidFields)
                    .exceptionType(ExceptionType.WEB)
                    .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(EntityExistsException.class)
    public ModelAndView handleEntityExistsException(EntityExistsException exception, HttpServletRequest request, HttpServletResponse response) {

        response.setStatus(400);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                .modelView("error")
                .status(400)
                .message(exception.getMessage())
                .exceptionType(ExceptionType.WEB)
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException exception, HttpServletRequest request, HttpServletResponse response) {

        response.setStatus(404);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                .modelView("error")
                .status(404)
                .message(exception.getMessage())
                .exceptionType(ExceptionType.WEB)
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletResponse response, HttpServletRequest request) {

        String invalidFields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + COLON_DELIMITER + fieldError.getDefaultMessage())
                .collect(Collectors.joining(COMMA_DELIMITER));

        response.setStatus(400);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                .modelView("error")
                .status(400)
                .message(invalidFields)
                .exceptionType(ExceptionType.WEB)
                .path(request.getRequestURI())
                .build());
    }
}
