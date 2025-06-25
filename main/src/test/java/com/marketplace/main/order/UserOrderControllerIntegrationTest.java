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
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserOrderControllerIntegrationTest {

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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/all")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("orders");
        List<OrderResponse> orderResponses = (List<OrderResponse>) model.get("orders");
        assertThat(orderResponses).isNotNull();
        assertThat(orderResponses.size()).isEqualTo(1);
        assertThat(orderResponses.get(0).getId()).isEqualTo(order.getId());
    }

    @Test
    public void getAllOrders_ShouldRedirectToHomePage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/all")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

    @Test
    public void getAllOrders_ShouldRedirectToHomePage_WhenNoAuth() throws Exception {
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/{id}", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("order");

        OrderResponse orderResponse = (OrderResponse) model.get("order");
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(order.getId());

        List<ProductResponse> productResponses = (List<ProductResponse>) model.get("products");
        assertThat(productResponses).isNotNull();
        assertThat(productResponses.size()).isEqualTo(1);
        assertThat(productResponses.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    public void getOrderById_ShouldRedirectToHomePage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/{id}", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

    @Test
    public void getOrderById_ShouldRedirectToHomePage_WhenNoAuth() throws Exception {
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.save(product);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .status(OrderStatus.IN_PROGRESS)
                .build();

        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("user-order");

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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(jwtCookiePayload.getAccessCookie()))
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

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.save(product);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .status(OrderStatus.COMPLETED)
                .build();

        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/user-order")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("user-order");

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

    @Test
    public void getUpdateOrder_ShouldReturnUpdateOrder_WhenRoleAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(get("/orders/{id}/update", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().isOk())
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("order-edit");

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        OrderResponse orderResponse = (OrderResponse) model.get("order");

        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(order.getId());
    }

    @Test
    public void getUpdateOrder_ShouldRedirectToHomePage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(get("/orders/{id}/update", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");
    }

    @Test
    public void updateOrder_ShouldUpdateOrder_WhenRoleAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(put("/orders/{id}/update", order.getId())
                        .param("address", "Hokkaido")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/orders/" + order.getId());

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getId()).isEqualTo(order.getId());
        assertThat(orderOptional.get().getAddress()).isEqualTo("Hokkaido");
    }

    @Test
    public void updateOrder_ShouldRedirectToHomePage_WhenRoleUser() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .productIds(Set.of(product.getId()))
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(put("/orders/{id}/update", order.getId())
                        .param("address", "Hokkaido")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getId()).isEqualTo(order.getId());
        assertThat(orderOptional.get().getAddress()).isEqualTo(order.getAddress());
    }

    @Test
    public void addProductToOrder_ShouldCreateAndAddProductToOrder_WhenRoleUserAndOrderNotExists() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.save(product);

        String redirectedUrl = mockMvc.perform(put("/orders/add-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/orders/user-order");

        Optional<Order> orderOptional = orderRepository.findOrderByOwnerId(authUser.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(orderOptional.get().getProductIds()).isNotNull();
        assertThat(orderOptional.get().getProductIds().size()).isEqualTo(1);
        assertThat(orderOptional.get().getProductIds().stream().anyMatch(productId -> productId.equals(product.getId()))).isTrue();
    }

    @Test
    public void addProductToOrder_ShouldAddProductToOrder_WhenRoleUserAndOrderExists() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.saveAll(List.of(product, product1));
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .status(OrderStatus.IN_PROGRESS)
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(put("/orders/add-product/{id}", product1.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/orders/user-order");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getProductIds()).isNotNull();
        assertThat(orderOptional.get().getProductIds().size()).isEqualTo(2);
        assertThat(orderOptional.get().getProductIds().stream().anyMatch(productId -> productId.equals(product1.getId()))).isTrue();
    }

    @Test
    public void addProductToOrder_ShouldRedirectToErrorPage_WhenRoleUserAndProductNotExists() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        String product1 = String.valueOf(UUID.randomUUID());

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.saveAll(List.of(product));
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .status(OrderStatus.IN_PROGRESS)
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(put("/orders/add-product/{id}", product1)
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
    }

    @Test
    public void addProductToOrder_ShouldRedirectToErrorPage_WhenRoleUserAndProductAmountIsNotEnough() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .amount(0)
                .build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(put("/orders/add-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");

        Optional<Order> orderOptional = orderRepository.findOrderByOwnerId(authUser.getId());
        assertThat(orderOptional).isNotPresent();
    }

    @Test
    public void deleteOrder_ShouldDeleteOrder_WhenRoleAdmin() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields()
                .role(UserRole.ADMIN)
                .build();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(delete("/orders/{id}/delete", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isNotPresent();
    }

    @Test
    public void deleteOrder_ShouldDeleteOrder_WhenRoleUserAndOrderOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .ownerId(authUser.getId())
                .build();
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(delete("/orders/{id}/delete", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isNotPresent();
    }

    @Test
    public void deleteOrder_ShouldRedirectToHomePage_WhenRoleUserAndNotOrderOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields().build();

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(delete("/orders/{id}/delete", order.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
    }

    @Test
    public void deleteOrder_ShouldRedirectToErrorPage_WhenRoleAdminAndOrderNotFound() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        String orderId = String.valueOf(UUID.randomUUID());

        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(delete("/orders/{id}/delete", orderId)
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
    }

    @Test
    public void removeProductFromOrder_ShouldRemoveProductFromOrder_WhenRoleUserAndOrderOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId(), product1.getId()))
                .build();
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(delete("/orders/remove-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getId()).isEqualTo(order.getId());
        assertThat(orderOptional.get().getProductIds()).isNotNull();
        assertThat(orderOptional.get().getProductIds().size()).isEqualTo(1);
        assertThat(orderOptional.get().getProductIds().stream().anyMatch(productId -> productId.equals(product1.getId()))).isTrue();
    }

    @Test
    public void removeProductFromOrder_ShouldDeleteOrderWhenTheLastProductRemoved_WhenRoleUserAndOrderOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(delete("/orders/remove-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isNotPresent();
    }

    @Test
    public void removeProductFromOrder_ShouldRedirectToErrorPage_WhenRoleUserAndOrderNotExist() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(delete("/orders/remove-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("Order not found!");
    }

    @Test
    public void removeProductFromOrder_ShouldRedirectToErrorPage_WhenRoleUserAndOrderOwnerAndOrderCompleted() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(delete("/orders/remove-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("Order not found!");
    }

    @Test
    public void payForOrder_ShouldPayForOrder_WhenUserRoleAndOrderOwner() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        String redirectedUrl = mockMvc.perform(post("/orders/user-order/pay")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).isEqualTo("/orders/user-order");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    public void payForOrder_ShouldRedirectToErrorPage_WhenUserRoleAndOrderNotExist() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(post("/orders/user-order/pay")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("Order not found!");
    }

    @Test
    public void payForOrder_ShouldRedirectToErrorPage_WhenUserRoleAndOrderCompleted() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        MvcResult mvcResult = mockMvc.perform(post("/orders/user-order/pay")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("Order not found!");
    }

    @Test
    public void payForOrder_ShouldRedirectToErrorPage_WhenOrderHasUnactiveProduct() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .active(false)
                .build();

        productRepository.save(product);
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId()))
                .build();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(post("/orders/user-order/pay")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("This product is not available");
    }

    @Test
    public void payForOrder_ShouldPayForOrder_WhenOrderHasUnactiveProductButUnactiveProductWasRemoved() throws Exception {
        User authUser = UserDataBuilder.buildUserWithAllFields().build();
        Product product = ProductDataBuilder.buildProductWithAllFields()
                .active(false)
                .build();
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();

        productRepository.saveAll(List.of(product, product1));
        AuthHelper.JwtCookiePayload jwtCookiePayload = authHelper.signUp(authUser, mockMvc);

        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.IN_PROGRESS)
                .ownerId(authUser.getId())
                .productIds(Set.of(product.getId(), product1.getId()))
                .build();
        orderRepository.save(order);

        MvcResult mvcResult = mockMvc.perform(post("/orders/user-order/pay")
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Map<String, Object> model = authHelper.requireModel(mvcResult);
        String errorMessage = model.get("message").toString();
        String viewName = authHelper.requireViewName(mvcResult);

        assertThat(viewName).isEqualTo("error");
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage).isEqualTo("This product is not available");

        String redirectedUrl = mockMvc.perform(delete("/orders/remove-product/{id}", product.getId())
                        .cookie(jwtCookiePayload.getAccessCookie()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(redirectedUrl).isEqualTo("/home");

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
        assertThat(orderOptional.get().getId()).isEqualTo(order.getId());
        assertThat(orderOptional.get().getProductIds()).isNotNull();
        assertThat(orderOptional.get().getProductIds().size()).isEqualTo(1);
        assertThat(orderOptional.get().getProductIds().stream().anyMatch(productId -> productId.equals(product1.getId()))).isTrue();
    }
}
