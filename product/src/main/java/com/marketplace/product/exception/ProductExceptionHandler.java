package com.marketplace.product.exception;

import com.marketplace.product.web.dto.ProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;


@Slf4j
@ControllerAdvice
public class ProductExceptionHandler {

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
