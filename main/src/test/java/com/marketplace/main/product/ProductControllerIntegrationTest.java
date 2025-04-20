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
                .forEach(MongoRepository::deleteAll);
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

        List<Product> products = objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.get(0).getId()).isEqualTo(product.getId());
    }

//    @Test
//    void getProductById_ShouldReturnProduct() throws Exception {
//        Product product = new Product();
//        product.setName("Test Product");
//        product.setDescription("Test Description");
//        product.setPrice(BigDecimal.valueOf(99.99));
//        product = productRepository.save(product);
//
//        mockMvc.perform(get("/product/" + product.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(product.getId())))
//                .andExpect(jsonPath("$.name", is("Test Product")));
//    }
//
//    @Test
//    void getProductById_ShouldReturn404_WhenNotFound() throws Exception {
//        String invalidId = "nonexistent-id-123";
//
//        mockMvc.perform(get("/product/" + invalidId))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message", containsString("not found")));
//    }
//
//    @Test
//    void createProduct_ShouldReturnCreatedProduct() throws Exception {
//        Product product = new Product();
//        product.setName("New Product");
//        product.setDescription("Some Description");
//        product.setPrice(BigDecimal.valueOf(49.99));
//
//        mockMvc.perform(post("/product/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(product)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("New Product")))
//                .andExpect(jsonPath("$.description", is("Some Description")));
//    }
//
//    @Test
//    void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
//        Product product = new Product();
//        product.setName("Old Product");
//        product.setDescription("Old Desc");
//        product.setPrice(BigDecimal.valueOf(10.00));
//        product = productRepository.save(product);
//
//        product.setName("Updated Product");
//        product.setDescription("Updated Desc");
//        product.setPrice(BigDecimal.valueOf(123.45));
//
//        mockMvc.perform(put("/product/" + product.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(product)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("Updated Product")))
//                .andExpect(jsonPath("$.description", is("Updated Desc")));
//    }
//
//    @Test
//    void deleteProduct_ShouldRemoveProduct() throws Exception {
//        Product product = new Product();
//        product.setName("To Delete");
//        product.setDescription("Delete Desc");
//        product.setPrice(BigDecimal.valueOf(12.34));
//        product = productRepository.save(product);
//
//        mockMvc.perform(delete("/product/" + product.getId()))
//                .andExpect(status().isOk());
//
//
//        mockMvc.perform(get("/product/" + product.getId()))
//                .andExpect(status().isNotFound());
//    }
}
