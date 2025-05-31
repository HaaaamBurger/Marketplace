package com.marketplace.main.exception;

import com.marketplace.auth.exception.*;
import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.common.exception.ExceptionType;
import com.marketplace.product.exception.ProductAmountNotEnoughException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
public class MainExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolationException(ConstraintViolationException exception, HttpServletRequest request) {

        String invalidFields = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(COMMA_DELIMITER));

        return buildErrorResponseModelAndView(
                400,
                invalidFields,
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(EntityExistsException.class)
    public ModelAndView handleEntityExistsException(EntityExistsException exception, HttpServletRequest request, HttpServletResponse response) {

        response.setStatus(400);

        return buildErrorResponseModelAndView(
                400,
                exception.getMessage(),
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException exception, HttpServletRequest request, HttpServletResponse response) {

        response.setStatus(404);

        return buildErrorResponseModelAndView(
                404,
                exception.getMessage(),
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(CredentialException.class)
    public ModelAndView handleCredentialsException(CredentialException exception, HttpServletResponse response, HttpServletRequest request) {

        response.setStatus(401);

        return buildErrorResponseModelAndView(
                401,
                exception.getMessage(),
                ExceptionType.AUTHORIZATION,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ProductAmountNotEnoughException.class)
    public ModelAndView handleProductAmountNotEnoughException(ProductAmountNotEnoughException exception, HttpServletResponse response, HttpServletRequest request) {

        response.setStatus(400);

        return buildErrorResponseModelAndView(
                400,
                exception.getMessage(),
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ModelAndView handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletResponse response, HttpServletRequest request) {

        String invalidFields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + COLON_DELIMITER + fieldError.getDefaultMessage())
                .collect(Collectors.joining(COMMA_DELIMITER));

        response.setStatus(400);

        return buildErrorResponseModelAndView(
                400,
                invalidFields,
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    private ModelAndView buildErrorResponseModelAndView(int status, String message, ExceptionType exceptionType, String path) {
        log.error("[MAIN_EXCEPTION_HANDLER]: {}", message);

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status", status);
        modelAndView.addObject("message", message);
        modelAndView.addObject("type", exceptionType);
        modelAndView.addObject("path", path);
        return modelAndView;
    }

}
