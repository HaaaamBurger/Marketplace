package com.marketplace.main.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.util.AuthHelper;
import com.marketplace.main.util.ProductDataBuilder;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.marketplace.auth.security.JwtService.AUTHORIZATION_HEADER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private ApplicationContext applicationContext;

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);

        String response = mockMvc.perform(get("/products")
                        .header(AUTHORIZATION_HEADER, authHelper.createUserAuth()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<Product> responseProducts = objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(responseProducts).isNotNull();
        assertThat(responseProducts.size()).isEqualTo(1);
        assertThat(responseProducts.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);

        String response = mockMvc.perform(get("/products/" + product.getId())
                        .header(AUTHORIZATION_HEADER, authHelper.createUserAuth()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getId()).isEqualTo(product.getId());
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        String response = mockMvc.perform(post("/products")
                        .header(AUTHORIZATION_HEADER, authHelper.createUserAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
        assertThat(responseProduct.getDescription()).isEqualTo(product.getDescription());
        assertThat(responseProduct.getPrice()).isEqualTo(product.getPrice());
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("Updated Product")
                .build();

        product = productRepository.save(product);

        String response = mockMvc.perform(put("/products/" + product.getId())
                        .header(AUTHORIZATION_HEADER, authHelper.createUserAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(product.getId()).isEqualTo(responseProduct.getId());
        assertThat(responseProduct.getName()).isEqualTo("Updated Product");
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        product = productRepository.save(product);

        assertThat(productRepository.findById(product.getId())).isPresent();

        mockMvc.perform(delete("/products/" + product.getId())
                        .header(AUTHORIZATION_HEADER, authHelper.createUserAuth()))
                .andExpect(status().isOk());

        assertThat(productRepository.findById(product.getId())).isNotPresent();
    }
}
