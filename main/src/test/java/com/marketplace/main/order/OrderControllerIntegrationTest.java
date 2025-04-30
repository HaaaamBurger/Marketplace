package com.marketplace.main.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.repository.UserRepository;
import com.marketplace.auth.security.JwtService;
import com.marketplace.auth.web.model.User;
import com.marketplace.main.util.OrderDataBuilder;
import com.marketplace.main.util.UserDataBuilder;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
import com.marketplace.order.web.model.OrderStatus;
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
import static com.marketplace.auth.security.JwtService.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    void getAllOrders_ShouldReturnList() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        userRepository.save(user);


        Order order = OrderDataBuilder.defaultOrder(user.getId()).status(OrderStatus.CREATED).build();
        orderRepository.save(order);

        String accessToken = jwtService.generateAccessToken(user);

        String response = mockMvc.perform(get("/orders/all")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<OrderResponse> responseOrders = objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(responseOrders).isNotNull();
        assertThat(responseOrders.size()).isEqualTo(1);
        assertThat(responseOrders.get(0).getUserId()).isEqualTo(order.getUserId());
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        userRepository.save(user);


        OrderRequest orderRequest = OrderDataBuilder.defaultOrderRequest()
                .status(OrderStatus.IN_PROGRESS)
                .build();
        String accessToken = jwtService.generateAccessToken(user);

        String response = mockMvc.perform(post("/orders/create")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        OrderResponse responseOrder = objectMapper.readValue(response, OrderResponse.class);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getUserId()).isEqualTo(user.getId());
        assertThat(responseOrder.getProductIds()).isEqualTo(orderRequest.getProductIds());
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        userRepository.save(user);

        Order order = OrderDataBuilder.defaultOrder(user.getId()).status(OrderStatus.COMPLETED).build();
        order = orderRepository.save(order);

        String accessToken = jwtService.generateAccessToken(user);

        String response = mockMvc.perform(get("/orders/" + order.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        OrderResponse responseOrder = objectMapper.readValue(response, OrderResponse.class);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getId()).isEqualTo(order.getId());
    }

    @Test
    void updateOrder_ShouldUpdateOrder() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        userRepository.save(user);

        Order order = OrderDataBuilder.defaultOrder(user.getId()).status(OrderStatus.SHIPPED).build();
        order = orderRepository.save(order);

        OrderRequest orderRequest = OrderDataBuilder.defaultOrderRequest()
                .address("Updated address")
                .status(OrderStatus.SHIPPED)
                .build();

        String accessToken = jwtService.generateAccessToken(user);

        String response = mockMvc.perform(put("/orders/" + order.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        OrderResponse responseOrder = objectMapper.readValue(response, OrderResponse.class);

        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getAddress()).isEqualTo("Updated address");
        assertThat(responseOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);

    }

    @Test
    void deleteOrder_ShouldRemoveOrder() throws Exception {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        userRepository.save(user);

        Order order = OrderDataBuilder.defaultOrder(user.getId()).status(OrderStatus.CREATED).build();
        order = orderRepository.save(order);

        String accessToken = jwtService.generateAccessToken(user);

        mockMvc.perform(delete("/orders/" + order.getId())
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isOk());

        assertThat(orderRepository.findById(order.getId())).isNotPresent();
    }
}
