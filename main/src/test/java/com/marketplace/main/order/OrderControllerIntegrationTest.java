package com.marketplace.main.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.service.AuthHelper;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

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

//    @Test
//    public void getAllOrders_ShouldReturnAllOrders() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(get("/orders")
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        List<Order> responseOrders = objectMapper.readValue(response, new TypeReference<>() {});
//
//        assertThat(responseOrders).isNotNull();
//        assertThat(responseOrders.size()).isEqualTo(1);
//        assertThat(responseOrders.get(0).getId()).isEqualTo(order.getId());
//    }
//
//    @Test
//    public void getAllOrders_ShouldThrowException_WhenUserAuthentication() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(get("/orders")
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
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders");
//    }
//
//    @Test
//    public void getOrderById_ShouldReturnOrder() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Order order = OrderDataBuilder.buildOrderWithAllFields()
//                .ownerId(userAuth.getAuthUser().getId())
//                .build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(get("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, userAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getId()).isEqualTo(order.getId());
//        assertThat(responseOrder.getOwnerId()).isEqualTo(userAuth.getAuthUser().getId());
//    }
//
//    @Test
//    public void getOrderById_ShouldThrowException_WhenUserAuthentication_AndUserNotOwner() throws Exception {
//        AuthHelper.AuthHelperResponse userAuth = authHelper.createUserAuth();
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(get("/orders/{orderId}", order.getId())
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
//        assertThat(exceptionResponse.getPath()).isEqualTo("/orders/%s", order.getId());
//    }
//
//    @Test
//    public void getOrderById_ShouldReturnOrder_WhenAdminAuthentication_AndAdminNotOwner() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//
//        orderRepository.save(order);
//
//        String response = mockMvc.perform(get("/orders/{orderId}", order.getId())
//                        .header(AUTHORIZATION_HEADER, adminAuth.getToken()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        Order responseOrder = objectMapper.readValue(response, Order.class);
//
//        assertThat(responseOrder).isNotNull();
//        assertThat(responseOrder.getId()).isEqualTo(order.getId());
//    }
//
//    @Test
//    public void createOrder_ShouldReturnOrder() throws Exception {
//        AuthHelper.AuthHelperResponse adminAuth = authHelper.createAdminAuth();
//        Product product = ProductDataBuilder.buildProductWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildOrderWithAllFields()
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
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildOrderWithAllFields()
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
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields().build();
//        OrderRequest orderRequest = OrderRequestDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields()
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
//        Order order = OrderDataBuilder.buildOrderWithAllFields()
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
