package com.marketplace.aws.exception;

import com.marketplace.common.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class AwsExceptionHandler {

    @ExceptionHandler(AwsPhotoUploadException.class)
    public ModelAndView handleAwsPhotoUploadException(AwsPhotoUploadException exception, HttpServletResponse response, HttpServletRequest request) {

        response.setStatus(400);

        return buildErrorResponseModelAndView(
                400,
                exception.getMessage(),
                ExceptionType.WEB,
                request.getRequestURI()
        );
    }

    private ModelAndView buildErrorResponseModelAndView(int status, String message, ExceptionType exceptionType, String path) {
        log.error("[AWS_EXCEPTION_HANDLER]: {}", message);

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status", status);
        modelAndView.addObject("message", message);
        modelAndView.addObject("type", exceptionType);
        modelAndView.addObject("path", path);
        return modelAndView;
    }

}
