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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {

        product = ProductDataBuilder.buildProductWithAllFields().build();
        product = productRepository.save(product);

        String response = mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product[] products = objectMapper.readValue(response, Product[].class);

        assertEquals("Test Product", products[0].getName());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {

        product = ProductDataBuilder.buildProductWithAllFields().build();
        product = productRepository.save(product);

        String response = mockMvc.perform(get("/" + product.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product actual = objectMapper.readValue(response, Product.class);

        assertEquals("Test Product", actual.getName());
        assertEquals(product.getId(), actual.getId());
        assertEquals(product.getPrice(), actual.getPrice());
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product newProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("New Product")
                .description("New Desc")
                .price(BigDecimal.valueOf(59.99))
                .build();

        String response = mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product actual = objectMapper.readValue(response, Product.class);

        assertEquals("New Product", actual.getName());
        assertEquals("New Desc", actual.getDescription());
        assertEquals(BigDecimal.valueOf(59.99), actual.getPrice());
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {

        product = ProductDataBuilder.buildProductWithAllFields().build();
        product = productRepository.save(product);

        product.setName("Updated Name");

        String response = mockMvc.perform(put("/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product actual = objectMapper.readValue(response, Product.class);

        assertEquals("Updated Name", actual.getName());
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {

        product = ProductDataBuilder.buildProductWithAllFields().build();
        product = productRepository.save(product);

        mockMvc.perform(delete("/" + product.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + product.getId()))
                .andExpect(status().isNotFound());
    }
}
