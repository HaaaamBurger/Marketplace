package com.marketplace.main.web.controller;

import com.marketplace.common.exception.ExceptionType;
import com.marketplace.main.util.AuthHelper;
import com.marketplace.usercore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MainErrorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthHelper authHelper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void getError_ShouldReturnErrorPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/error"))
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");

        Map<String, Object> model = mvcResult.getModelAndView().getModel();
        assertThat(model).isNotNull();

        String status = model.get("status").toString();
        String message = model.get("message").toString();
        ExceptionType exceptionType = (ExceptionType) model.get("type");
        String path = model.get("path").toString();

        assertThat(status).isNotNull();
        assertThat(message).isNotNull();
        assertThat(exceptionType).isNotNull();
        assertThat(path).isNotNull();

        assertThat(status).isEqualTo(String.valueOf(SC_BAD_REQUEST));
        assertThat(message).isEqualTo("Something went wrong!");
        assertThat(exceptionType).isEqualTo(ExceptionType.SYSTEM);
        assertThat(path).isEqualTo("/error");
    }

    @Test
    public void getProductById_ShouldRedirectToErrorPage_WhenProductNotFound() throws Exception {
        String productId = String.valueOf(UUID.randomUUID());

        MvcResult mvcResult = mockMvc.perform(get("/products/{id}", productId))
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");

        Map<String, Object> model = mvcResult.getModelAndView().getModel();
        assertThat(model).isNotNull();

        String status = model.get("status").toString();
        String message = model.get("message").toString();
        ExceptionType exceptionType = (ExceptionType) model.get("type");
        String path = model.get("path").toString();

        assertThat(status).isNotNull();
        assertThat(message).isNotNull();
        assertThat(exceptionType).isNotNull();
        assertThat(path).isNotNull();

        assertThat(status).isEqualTo(String.valueOf(SC_NOT_FOUND));
        assertThat(message).isEqualTo("Product not found!");
        assertThat(exceptionType).isEqualTo(ExceptionType.WEB);
        assertThat(path).isEqualTo("/products/" + productId);
    }

}
