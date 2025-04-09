package com.marketplace.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.model.Product;
import com.marketplace.product.util.ProductDataBuilder;
import com.marketplace.product.web.rest.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        product = ProductDataBuilder.buildProductWithAllFields()
                .build();

        product = productRepository.save(product);
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product newProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("New Product")
                .description("New Desc")
                .price(BigDecimal.valueOf(59.99))
                .build();

        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        product.setName("Updated Name");

        mockMvc.perform(put("/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {
        mockMvc.perform(delete("/" + product.getId()))
                .andExpect(status().isOk());
    }
}
