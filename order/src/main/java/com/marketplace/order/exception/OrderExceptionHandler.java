package com.marketplace.order.exception;

import com.marketplace.common.exception.CommonExceptionService;
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
public class OrderExceptionHandler {

    private final CommonExceptionService commonExceptionService;

    @ExceptionHandler(OrderUpdateException.class)
    protected ModelAndView handleOrderUpdateException(OrderUpdateException exception, HttpServletResponse response, HttpServletRequest request) {
        log.error("[ORDER_EXCEPTION_HANDLER]: {}", exception.getMessage());

        response.setStatus(400);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                        .modelView("error")
                        .status(400)
                        .message(exception.getMessage())
                        .exceptionType(ExceptionType.WEB)
                        .path(request.getRequestURI())
                .build());
    }
}
