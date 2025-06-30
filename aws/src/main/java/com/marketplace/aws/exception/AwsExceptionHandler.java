package com.marketplace.aws.exception;

import com.marketplace.common.exception.ExceptionService;
import com.marketplace.common.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class AwsExceptionHandler {

    private final ExceptionService exceptionService;

    @ExceptionHandler(AwsPhotoUploadException.class)
    public ModelAndView handleAwsPhotoUploadException(AwsPhotoUploadException exception, HttpServletResponse response, HttpServletRequest request) {

        log.error("[AWS_EXCEPTION_HANDLER]: {}", exception.getMessage());

        response.setStatus(400);
        return exceptionService.buildErrorResponseModelAndView(ExceptionService.ErrorModelPayload.builder()
                .modelView("error")
                .status(400)
                .message(exception.getMessage())
                .exceptionType(ExceptionType.WEB)
                .path(request.getRequestURI())
                .build());
    }
}
