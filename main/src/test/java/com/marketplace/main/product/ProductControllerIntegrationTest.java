package com.marketplace.main.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.exception.ExceptionResponse;
import com.marketplace.auth.exception.ExceptionType;
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
import org.springframework.http.HttpStatus;
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
    public void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void getAllProducts_ShouldReturnList() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);

        String response = mockMvc.perform(get("/products")
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<Product> responseProducts = objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(responseProducts).isNotNull();
        assertThat(responseProducts.size()).isEqualTo(1);
        assertThat(responseProducts.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    public void getProductById_ShouldReturnProduct() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);

        String response = mockMvc.perform(get("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getId()).isEqualTo(product.getId());
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    public void getProductById_ShouldThrowException_WhenProductNotFound() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        String response = mockMvc.perform(get("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
        assertThat(exceptionResponse.getMessage()).isEqualTo("Product not found!");
        assertThat(exceptionResponse.getPath()).isEqualTo("/products/%s", product.getId());
    }

    @Test
    public void createProduct_ShouldReturnCreatedProduct() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        String response = mockMvc.perform(post("/products")
                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
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
    public void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(userAuth.getAuthUser().getId())
                .build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("Updated Product")
                .build();

        product = productRepository.save(product);

        String response = mockMvc.perform(put("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
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
    public void updateProduct_ShouldNotAllow_WhenUserNotOwner() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("Updated Product")
                .build();

        product = productRepository.save(product);

        String response = mockMvc.perform(put("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
        assertThat(exceptionResponse.getPath()).isEqualTo("/products/%s", product.getId());
    }

    @Test
    public void updateProduct_ShouldUpdate_WhenUserNotOwnerButAdmin() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createAdminAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
                .name("Updated Product")
                .build();

        product = productRepository.save(product);

        String response = mockMvc.perform(put("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Product responseProduct = objectMapper.readValue(response, Product.class);

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getName()).isEqualTo(updatedProduct.getName());
    }

    @Test
    public void deleteProduct_ShouldRemoveProduct() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(userAuth.getAuthUser().getId())
                .build();

        product = productRepository.save(product);
        assertThat(productRepository.findById(product.getId())).isPresent();

        mockMvc.perform(delete("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isOk());

        assertThat(productRepository.findById(product.getId())).isNotPresent();
    }

    @Test
    public void deleteProduct_ShouldNotAllow_WhenUserNotOwner() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        product = productRepository.save(product);

        String response = mockMvc.perform(delete("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);

        assertThat(exceptionResponse).isNotNull();
        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
        assertThat(exceptionResponse.getPath()).isEqualTo("/products/%s", product.getId());
    }

    @Test
    public void deleteProduct_ShouldRemove_WhenUserNotOwnerButAdmin() throws Exception {
        AuthHelper.AuthHelperResponse userAuth = authHelper.createAdminAuth();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        product = productRepository.save(product);

         mockMvc.perform(delete("/products/{productId}", product.getId())
                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
                .andExpect(status().isOk());

        assertThat(productRepository.findById(product.getId())).isNotPresent();
    }

}
