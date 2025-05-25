package com.marketplace.main.product;

import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.ProductDataBuilder;
import com.marketplace.main.util.builder.ProductRequestDataBuilder;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.product.repository.ProductRepository;

import com.marketplace.product.web.dto.ProductRequest;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    public void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void getAllProducts_ShouldReturnProducts() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        productRepository.saveAll(List.of(product, product1));

        MvcResult mvcResult = mockMvc.perform(get("/products")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("products");
        Assertions.assertThat(productResponses.size()).isEqualTo(2);
    }

    @Test
    public void getProductById_ShouldReturnProduct() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/products/{productId}", product.getId())
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        ProductResponse responseProduct = (ProductResponse) model.get("product");

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getId()).isEqualTo(product.getId());
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    public void getProductById_ShouldRedirectToErrorPage_WhenProductNotFound() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);

        ModelAndView modelAndView = mockMvc.perform(get("/products/{productId}", product.getId())
                        .cookie(cookie))
                .andExpect(status().isNotFound())
                .andReturn().getModelAndView();

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("error");
    }

    @Test
    public void createProduct_WhenUserAuthenticated_ShouldRedirectToProducts() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);

        String redirectedUrl = mockMvc.perform(post("/products/create")
                        .cookie(cookie)
                        .param("name", productRequest.getName())
                        .param("description", productRequest.getDescription())
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products");

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(authUser.getId());
        assertThat(productByOwnerId).isPresent();
    }

    @Test
    public void createProduct_WhenUserNotAuthenticated_ShouldRedirectToHome() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        String redirectedUrl = mockMvc.perform(post("/products/create")
                        .param("name", productRequest.getName())
                        .param("description", productRequest.getDescription())
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(authUser.getId());
        assertThat(productByOwnerId).isNotPresent();
    }

    @Test
    public void updateProduct_ShouldUpdateAndRedirectToProduct_WhenUserOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(authUser.getId())
                .build();
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(put("/products/{productId}/update", product.getId())
                        .cookie(cookie)
                        .param("name", productRequest.getName())
                        .param("description", productRequest.getDescription())
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products/%s", product.getId());
    }

//
//    @Test
//    public void updateProduct_ShouldUpdateAndReturnProduct() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
//                .name("Updated Product")
//                .build();
//
//        product = productRepository.save(product);
//
//        String response = mockMvc.perform(put("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProduct)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        Product responseProduct = objectMapper.readValue(response, Product.class);
//
//        assertThat(product.getId()).isEqualTo(responseProduct.getId());
//        assertThat(responseProduct.getName()).isEqualTo("Updated Product");
//    }
//
//    @Test
//    public void updateProduct_ShouldNotAllow_WhenUserNotOwner() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
//                .name("Updated Product")
//                .build();
//
//        product = productRepository.save(product);
//
//        String response = mockMvc.perform(put("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProduct)))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/products/%s", product.getId());
//    }
//
//    @Test
//    public void updateProduct_ShouldUpdate_WhenUserNotOwnerButAdmin() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createAdminAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        Product updatedProduct = ProductDataBuilder.buildProductWithAllFields()
//                .name("Updated Product")
//                .build();
//
//        product = productRepository.save(product);
//
//        String response = mockMvc.perform(put("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProduct)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        Product responseProduct = objectMapper.readValue(response, Product.class);
//
//        assertThat(responseProduct).isNotNull();
//        assertThat(responseProduct.getName()).isEqualTo(updatedProduct.getName());
//    }
//
//    @Test
//    public void deleteProduct_ShouldRemoveProduct() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//
//        product = productRepository.save(product);
//        assertThat(productRepository.findById(product.getId())).isPresent();
//
//        mockMvc.perform(delete("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
//                .andExpect(status().isOk());
//
//        assertThat(productRepository.findById(product.getId())).isNotPresent();
//    }
//
//    @Test
//    public void deleteProduct_ShouldNotAllow_WhenUserNotOwner() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//
//        product = productRepository.save(product);
//
//        String response = mockMvc.perform(delete("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/products/%s", product.getId());
//    }
//
//    @Test
//    public void deleteProduct_ShouldRemove_WhenUserNotOwnerButAdmin() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createAdminAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//
//        product = productRepository.save(product);
//
//         mockMvc.perform(delete("/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
//                .andExpect(status().isOk());
//
//        assertThat(productRepository.findById(product.getId())).isNotPresent();
//    }

}
