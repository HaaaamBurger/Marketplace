package com.marketplace.product.exception;

import com.marketplace.common.exception.CommonExceptionService;
import com.marketplace.common.exception.ExceptionType;
import com.marketplace.product.web.dto.ProductRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;


@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ProductExceptionHandler {

    private final CommonExceptionService commonExceptionService;

    @ExceptionHandler(ProductNotAvailableException.class)
    public ModelAndView handleProductAmountNotEnoughException(ProductNotAvailableException exception, HttpServletResponse response, HttpServletRequest request) {
        log.error("[PRODUCT_EXCEPTION_HANDLER]: {}", exception.getMessage());

        response.setStatus(400);
        return commonExceptionService.buildErrorResponseModelAndView(CommonExceptionService.ErrorModelPayload.builder()
                .modelView("error")
                .status(400)
                .message(exception.getMessage())
                .exceptionType(ExceptionType.WEB)
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceededException(Model model) {

        model.addAttribute("productRequest", ProductRequest.builder()
                .active(true)
                .price(BigDecimal.ZERO)
                .amount(1)
                .build());

        ModelAndView modelAndView = new ModelAndView("product-create");
        modelAndView.addObject("photoSizeError", "Photo size can't be more than 5MB");

        return modelAndView;
    }
}
