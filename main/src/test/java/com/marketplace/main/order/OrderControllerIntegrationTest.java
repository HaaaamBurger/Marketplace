package com.marketplace.main.order;

import com.marketplace.main.util.AuthHelper;
import com.marketplace.main.util.builder.OrderDataBuilder;
import com.marketplace.main.util.builder.ProductDataBuilder;
import com.marketplace.main.util.builder.UserDataBuilder;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.dto.OrderResponse;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.dto.ProductResponse;
import com.marketplace.product.web.model.Product;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserRole;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AuthHelper authHelper;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void getAllOrders_ShouldReturnAllOrders_WhenRoleAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/all")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        assertThat(mvcResult.getModelAndView().getViewName()).isNotNull();
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("orders");
        List<OrderResponse> orderResponses = (List<OrderResponse>) model.get("orders");
        assertThat(orderResponses).isNotNull();
        assertThat(orderResponses.size()).isEqualTo(1);
        assertThat(orderResponses.get(0).getId()).isEqualTo(order.getId());
    }

    @Test
    public void getAllOrders_ShouldRedirectToErrorPage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/all")
                        .cookie(cookie))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");
    }

    @Test
    public void getAllOrders_ShouldRedirectToErrorPage_WhenNoAuth() throws Exception {
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/all"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

    @Test
    public void getOrderById_ShouldReturnOrder_WhenRoleAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/{id}", order.getId())
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        assertThat(mvcResult.getModelAndView().getViewName()).isNotNull();
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("order");

        OrderResponse orderResponse = (OrderResponse) model.get("order");
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(order.getId());

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("products");
        assertThat(productResponses).isNotNull();
        assertThat(productResponses.size()).isEqualTo(1);
        assertThat(productResponses.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    public void getOrderById_ShouldRedirectToErrorPage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/{id}", order.getId())
                        .cookie(cookie))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/error");
    }

    @Test
    public void getOrderById_ShouldRedirectToErrorPage_WhenNoAuth() throws Exception {
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/{id}", order.getId()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

    @Test
    public void getUserOrder_ShouldReturnUserOrder_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .status(OrderStatus.IN_PROGRESS)
                .build();

        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        assertThat(mvcResult.getModelAndView().getViewName()).isNotNull();
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("user-order");

        OrderResponse orderResponse = (OrderResponse) model.get("currentOrder");
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(order.getId());

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("orderProducts");
        assertThat(productResponses).isNotNull();
        assertThat(productResponses.size()).isEqualTo(1);
        assertThat(productResponses.get(0).getId()).isEqualTo(product.getId());

        BigDecimal totalSum = (BigDecimal) model.get("totalSum");
        assertThat(totalSum).isNotNull();
        assertThat(totalSum).isEqualTo(product.getPrice());
    }

    @Test
    public void getUserOrder_ShouldReturnUserOrder_WhenRoleUserAndNoActiveOrder() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        assertThat(mvcResult.getModelAndView().getViewName()).isNotNull();
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("user-order");

        OrderResponse orderResponse = (OrderResponse) model.get("currentOrder");
        assertThat(orderResponse).isNull();

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("orderProducts");
        assertThat(productResponses).isNull();

        BigDecimal totalSum = (BigDecimal) model.get("totalSum");
        assertThat(totalSum).isNull();


    }

    @Test
    public void getUserOrder_ShouldReturnUserOrder_WhenRoleUserAndOneHistoryOrder() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        Cookie cookie = authHelper.signIn(authUser, mockMvc);
        productRepository.save(product);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .status(OrderStatus.COMPLETED)
                .build();

        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);

        assertThat(mvcResult.getModelAndView().getViewName()).isNotNull();
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("user-order");

        OrderResponse orderResponse = (OrderResponse) model.get("currentOrder");
        assertThat(orderResponse).isNull();

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("orderProducts");
        assertThat(productResponses).isNull();

        BigDecimal totalSum = (BigDecimal) model.get("totalSum");
        assertThat(totalSum).isNull();

        List<OrderResponse> orderHistoryResponses = (List<OrderResponse>) model.get("historyOrders");
        assertThat(orderHistoryResponses).isNotNull();
        assertThat(orderHistoryResponses.size()).isEqualTo(1);
        assertThat(orderHistoryResponses.get(0).getId()).isEqualTo(order.getId());
        assertThat(orderHistoryResponses.get(0).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

//    @Test
//    public void createOrder_ShouldReturnOrder() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildProductWithAllFields()
//                .productIds(List.of(product.getId()))
//                .build();
//
//        productRepository.save(product);
//
//        String response = mockMvc.perform(post("/orders")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderRequest)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getProductIds()).isNotNull();
//        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
//        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(product.getId());
//        assertThat(responseOrder.getAddress()).isEqualTo(orderRequest.getAddress());
//        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
//        assertThat(responseOrder.getCreatedAt()).isNotNull();
//        assertThat(responseOrder.getUpdatedAt()).isNotNull();
//    }
//
//    @Test
//    public void createOrder_ShouldThrowException_WhenUserAuthentication() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildProductWithAllFields()
//                .productIds(List.of(product.getId()))
//                .build();
//
//        productRepository.save(product);
//
//        String response = mockMvc.perform(post("/orders")
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderRequest)))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders");
//    }
//
//    @Test
//    public void createOrder_ShouldThrowException_WhenProductNotFound() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildProductWithAllFields()
//                .productIds(List.of(product.getId()))
//                .build();
//
//        String response = mockMvc.perform(post("/orders")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderRequest)))
//                .andExpect(status().isNotFound())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Product not found!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders");
//    }
//
//    @Test
//    public void updateOrder_ShouldUpdateOrder() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Order order = OrderDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildProductWithAllFields()
//                .status(OrderStatus.COMPLETED)
//                .build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(put("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getStatus()).isEqualTo(orderRequest.getStatus());
//    }
//
//    @Test
//    public void updateOrder_ShouldThrowException_WhenUserAuthentication() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Order order = OrderDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildProductWithAllFields()
//                .status(OrderStatus.COMPLETED)
//                .build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(put("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderRequest)))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders/%s", order.getId());
//    }
//
//    @Test
//    public void addProductToOrder_ShouldAddProductToOrder() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//
//        productRepository.save(product);
//
//        String response = mockMvc.perform(put("/orders/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getProductIds()).isNotNull();
//        assertThat(responseOrder.getProductIds().size()).isEqualTo(1);
//        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(product.getId());
//        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
//    }
//
//    @Test
//    public void addProductToOrder_ShouldAddProductToExistingOrder() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//        Product product1 = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//        Product product2 = ProductDataBuilder.buildProductWithAllFields().build();
//        Order order = OrderDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .productIds(List.of(product.getId()))
//                .build();
//
//        orderRepository.save(order);
//        productRepository.saveAll(List.of(product, product1, product2));
//
//        String response = mockMvc.perform(put("/orders/products/{productId}", product1.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getProductIds()).isNotNull();
//        assertThat(responseOrder.getProductIds().size()).isEqualTo(2);
//        assertThat(responseOrder.getProductIds().get(0)).isEqualTo(product.getId());
//        assertThat(responseOrder.getProductIds().get(1)).isEqualTo(product1.getId());
//    }
//
//    @Test
//    public void addProductToOrder_ShouldThrowException_WhenProductNotFound() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//
//        String response = mockMvc.perform(put("/orders/products/{productId}", product.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Product not found!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders/products/%s", product.getId());
//    }
//
//    @Test
//    public void deleteOrder_ShouldDeleteOrder() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Order order = OrderDataBuilder.buildProductWithAllFields()
//                .ownerId(adminAuth.getAuthUser().getId())
//                .build();
//
//        orderRepository.save(order);
//
//        mockMvc.perform(delete("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//
//        assertThat(orderRepository.findById(order.getId())).isEmpty();
//    }
//
//    @Test
//    public void deleteOrder_ShouldThrowException_WhenUserAuthentication() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Order order = OrderDataBuilder.buildProductWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(delete("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.AUTHORIZATION);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Forbidden, not enough access!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders/%s", order.getId());
//    }
//
//    @Test
//    public void deleteOrder_ShouldThrowException_WhenOrderNotFound() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Order order = OrderDataBuilder.buildProductWithAllFields()
//                .ownerId(adminAuth.getAuthUser().getId())
//                .build();
//
//        String response = mockMvc.perform(delete("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andReturn().getResponse().getContentAsString();
//
//        ExceptionResponse exceptionResponse = objectMapper.readValue(response, ExceptionResponse.class);
//
//        assertThat(exceptionResponse).isNotNull();
//        assertThat(exceptionResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
//        assertThat(exceptionResponse.getType()).isEqualTo(ExceptionType.WEB);
//        assertThat(exceptionResponse.getMessage()).isEqualTo("Order not found!");
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders/%s", order.getId());
//    }

}
