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
import com.marketplace.usercore.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

import java.net.URL;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        productRepository.saveAll(List.of(product, product1));

        MvcResult mvcResult = mockMvc.perform(get("/products/all")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        Set<ProductResponse> productResponses = (Set<ProductResponse>) model.get("products");
        assertThat(productResponses).isNotNull();
        assertThat(productResponses.size()).isEqualTo(2);
    }

    @Test
    public void getAllProducts_WhenNoAuth_ShouldReturnProducts() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.saveAll(List.of(product, product1));

        MvcResult mvcResult = mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        Set<ProductResponse> productResponses = (Set<ProductResponse>) model.get("products");
        assertThat(productResponses).isNotNull();
        assertThat(productResponses.size()).isEqualTo(2);
    }

    @Test
    public void getProductById_ShouldReturnProduct() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/products/{productId}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        ModelAndView modelAndView = mockMvc.perform(get("/products/{productId}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isNotFound())
                .andReturn().getModelAndView();

        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("error");
    }

    @Test
    public void getProductById_ShouldReturnProduct_WhenNoAuth() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/products/{productId}", product.getId()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        ProductResponse responseProduct = (ProductResponse) model.get("product");

        assertThat(responseProduct).isNotNull();
        assertThat(responseProduct.getId()).isEqualTo(product.getId());
        assertThat(responseProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    public void createProduct_ShouldRedirectToProducts_WhenUserAuthenticated() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        String redirectedUrl = mockMvc.perform(multipart("/products/create")
                        .file((MockMultipartFile) productRequest.getPhoto())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products/all");

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(authUser.getId());
        assertThat(productByOwnerId).isPresent();
        assertThat(productByOwnerId.get().getName()).isEqualTo(productRequest.getName());
        assertThat(productByOwnerId.get().getAmount()).isEqualTo(productRequest.getAmount());
        assertThat(productByOwnerId.get().getOwnerId()).isEqualTo(authUser.getId());
        assertThat(productByOwnerId.get().getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(productByOwnerId.get().getActive()).isNotNull();
        assertThat(productByOwnerId.get().getActive()).isTrue();
        assertThat(productByOwnerId.get().getPhotoUrl()).isNotNull();
        assertThat(new URL(productByOwnerId.get().getPhotoUrl())).isNotNull();
    }

    @Test
    public void createProduct_ShouldRedirectToProduct_WhenPhotoExtensionIsUnsupported() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields()
                .photo(new MockMultipartFile("photo", "photo.svg", "image/svg", "photo".getBytes()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(multipart("/products/create")
                        .file((MockMultipartFile) productRequest.getPhoto())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        FieldError fieldError = ((BindingResult) modelAndView.getModel().get("org.springframework.validation.BindingResult.productRequest")).getFieldError();
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isEqualTo("Unsupported photo extension: .svg");
    }

    @Test
    public void createProduct_ShouldRedirectToProduct_WhenPhotoWithoutExtension() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields()
                .photo(new MockMultipartFile("photo", "photo", "image/svg", "photo".getBytes()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(multipart("/products/create")
                        .file((MockMultipartFile) productRequest.getPhoto())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        FieldError fieldError = ((BindingResult) modelAndView.getModel().get("org.springframework.validation.BindingResult.productRequest")).getFieldError();
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isEqualTo("File name is missing or has no valid extension");
    }

    @Test
    public void createProduct_WhenValidationError_ShouldRedirectToProduct() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields()
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(post("/products/create")
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", "-1"))
                .andExpect(status().isOk())
                .andReturn();


        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("product-create");

        FieldError fieldError = ((BindingResult) modelAndView.getModel().get("org.springframework.validation.BindingResult.productRequest")).getFieldError();
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isEqualTo("Price must be greater or equal to 0");
    }

    @Test
    public void createProduct_WhenUserNotAuthenticated_ShouldRedirectToHome() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        String redirectedUrl = mockMvc.perform(post("/products/create")
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(authUser.getId())
                .build();
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(put("/products/{productId}/update", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products/%s", product.getId());

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(product.getOwnerId());
        assertThat(productByOwnerId).isPresent();
        assertThat(productByOwnerId.get().getOwnerId()).isEqualTo(authUser.getId());
        assertThat(productByOwnerId.get().getName()).isEqualTo(productRequest.getName());
        assertThat(productByOwnerId.get().getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(productByOwnerId.get().getPrice()).isEqualTo(productRequest.getPrice());
    }

    @Test
    public void updateProduct_ShouldUpdateAndRedirectToProduct_WhenUserAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(String.valueOf(UUID.randomUUID()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(put("/products/{productId}/update", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products/%s", product.getId());

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(product.getOwnerId());
        assertThat(productByOwnerId).isPresent();
        assertThat(productByOwnerId.get().getOwnerId()).isNotEqualTo(authUser.getId());
        assertThat(productByOwnerId.get().getName()).isEqualTo(productRequest.getName());
        assertThat(productByOwnerId.get().getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(productByOwnerId.get().getPrice()).isEqualTo(productRequest.getPrice());
    }

    @Test
    public void updateProduct_ShouldRedirectToHomePage_WhenUserNotAdminAndNotOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(String.valueOf(UUID.randomUUID()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(put("/products/{productId}/update", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(product.getOwnerId());
        assertThat(productByOwnerId).isPresent();
        assertThat(productByOwnerId.get().getName()).isEqualTo(product.getName());
        assertThat(productByOwnerId.get().getDescription()).isEqualTo(product.getDescription());
        assertThat(productByOwnerId.get().getPrice()).isEqualTo(product.getPrice());
    }

    @Test
    public void updateProduct_ShouldRedirectToUpdateProduct_WhenValidationError() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(String.valueOf(UUID.randomUUID()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(put("/products/{productId}/update", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", "")
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice())))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getViewName()).isNotNull();
        assertThat(modelAndView.getViewName()).isEqualTo("product-update");

        FieldError fieldError = ((BindingResult) modelAndView.getModel().get("org.springframework.validation.BindingResult.productRequest")).getFieldError();
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getDefaultMessage()).isNotNull();
        assertThat(fieldError.getDefaultMessage()).containsAnyOf("Name is required", "Name must be between 2 and 100 characters");
    }

    @Test
    public void updateProduct_ShouldUpdatePhotoAndRedirectToProduct_WhenUserOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        ProductRequest productRequest = ProductRequestDataBuilder.buildProductWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signIn(authUser, mockMvc);
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .ownerId(authUser.getId())
                .build();
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(multipart("/products/{productId}/update", product.getId())
                        .file((MockMultipartFile) productRequest.getPhoto())
                        .cookie(jwtCookiePayload.getAccessCookie())
                        .param("name", productRequest.getName())
                        .param("active", String.valueOf(productRequest.getActive()))
                        .param("description", productRequest.getDescription())
                        .param("amount", String.valueOf(productRequest.getAmount()))
                        .param("price", String.valueOf(productRequest.getPrice()))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/products/%s", product.getId());

        Optional<Product> productByOwnerId = productRepository.findProductByOwnerId(product.getOwnerId());

        assertThat(productByOwnerId).isPresent();
        assertThat(productByOwnerId.get().getPhotoUrl()).isNotNull();
    }
}
