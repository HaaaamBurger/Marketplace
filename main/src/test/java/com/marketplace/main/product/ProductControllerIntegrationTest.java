package com.marketplace.main.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.main.util.ProductDataBuilder;
import com.marketplace.main.util.UserDataBuilder;
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

import java.math.BigDecimal;
import java.util.List;

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
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    @BeforeEach
    void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(repo -> {
                    if (repo != null) {
                        repo.deleteAll();
                    }
                });
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        userRepository.save(user);
        productRepository.save(product);

        String accessToken = jwtService.generateAccessToken(user);
        String response = mockMvc.perform(get("/product/all")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<Product> products = objectMapper.readValue(response, new TypeReference<>() {
        });

        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        userRepository.save(user);
        productRepository.save(product);

        String accessToken = jwtService.generateAccessToken(user);
        String contentAsString = mockMvc.perform(get("/product/" + product.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(contentAsString, Product.class);

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getId()).isEqualTo(product.getId());
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);

        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        String response = mockMvc.perform(post("/product/create")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
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
        User user = UserDataBuilder.buildUserWithAllFields().build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);

        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        product = productRepository.save(product);

        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("Updated Product")
                .description("Updated Desc")
                .price(BigDecimal.valueOf(123.45))
                .build();

        String response = mockMvc.perform(put("/product/" + product.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(responseProduct.getName()).isEqualTo("Updated Product");
        assertThat(responseProduct.getDescription()).isEqualTo("Updated Desc");
        assertThat(responseProduct.getPrice()).isEqualTo(BigDecimal.valueOf(123.45));
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);

        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        product = productRepository.save(product);

        assertThat(productRepository.findById(product.getId())).isPresent();

        mockMvc.perform(delete("/product/" + product.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk());

        assertThat(productRepository.findById(product.getId())).isEmpty();
    }
}
