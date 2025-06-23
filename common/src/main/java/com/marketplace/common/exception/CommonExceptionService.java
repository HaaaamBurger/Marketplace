package com.marketplace.common.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class CommonExceptionService {

    public ModelAndView buildErrorResponseModelAndView(ErrorModelPayload errorModelPayload) {

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status", errorModelPayload.getStatus());
        modelAndView.addObject("message", errorModelPayload.getMessage());
        modelAndView.addObject("type", errorModelPayload.getExceptionType());
        modelAndView.addObject("path", errorModelPayload.getPath());
        return modelAndView;
    }

    @Data
    @Builder
    public static class ErrorModelPayload {

        private String modelView;

        private int status;

        private String message;

        private ExceptionType exceptionType;

        private String path;

    }

}
