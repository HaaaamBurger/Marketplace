package com.marketplace.product.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.web.model.Product;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
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

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        productRepository.save(product);

        mockMvc.perform(get("/product/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product = productRepository.save(product);

        mockMvc.perform(get("/product/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(product.getId())))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void getProductById_ShouldReturn404_WhenNotFound() throws Exception {
        String invalidId = "nonexistent-id-123";

        mockMvc.perform(get("/product/" + invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product product = new Product();
        product.setName("New Product");
        product.setDescription("Some Description");
        product.setPrice(BigDecimal.valueOf(49.99));

        mockMvc.perform(post("/product/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.description", is("Some Description")));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
        Product product = new Product();
        product.setName("Old Product");
        product.setDescription("Old Desc");
        product.setPrice(BigDecimal.valueOf(10.00));
        product = productRepository.save(product);

        product.setName("Updated Product");
        product.setDescription("Updated Desc");
        product.setPrice(BigDecimal.valueOf(123.45));

        mockMvc.perform(put("/product/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.description", is("Updated Desc")));
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {
        Product product = new Product();
        product.setName("To Delete");
        product.setDescription("Delete Desc");
        product.setPrice(BigDecimal.valueOf(12.34));
        product = productRepository.save(product);

        mockMvc.perform(delete("/product/" + product.getId()))
                .andExpect(status().isOk());


        mockMvc.perform(get("/product/" + product.getId()))
                .andExpect(status().isNotFound());
    }
}
