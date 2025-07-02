package com.marketplace.main.order.kafka.consumer;

import com.marketplace.main.util.TestListener;
import com.marketplace.main.util.builder.OrderDataBuilder;
import com.marketplace.main.util.builder.ProductDataBuilder;
import com.marketplace.main.util.TestSender;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.web.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ProductEventConsumerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestSender testSender;

    @Autowired
    private TestListener testListener;

    @BeforeEach
    public void setUp() {
        applicationContext.getBeansOfType(MongoRepository.class)
                .values()
                .forEach(MongoRepository::deleteAll);
    }

    @Test
    public void sendDeleteProductFromOrdersEvent_ShouldDeleteProductAndProductFromOrders() {
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Product product2 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order1 = OrderDataBuilder.buildOrderWithAllFields()
                .products(Set.of(product1, product2))
                .build();
        Order order2 = OrderDataBuilder.buildOrderWithAllFields()
                .products(Set.of(product2))
                .build();

        productRepository.save(product1);
        orderRepository.saveAll(List.of(order1, order2));

        testSender.sendDeleteProductFromOrdersEvent(product1.getId());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Product> byId = productRepository.findById(product1.getId());
            assertThat(byId).isNotPresent();
        });

        Optional<Order> byId1 = orderRepository.findById(order1.getId());
        assertThat(byId1).isPresent();
        assertThat(byId1.get().getProducts()).isNotNull();
        assertThat(byId1.get().getProducts().size()).isEqualTo(1);
        assertThat(byId1.get().getProducts().contains(product2)).isTrue();

        Optional<Order> byId2 = orderRepository.findById(order2.getId());
        assertThat(byId2).isPresent();
        assertThat(byId2.get().getProducts()).isNotNull();
        assertThat(byId2.get().getProducts().size()).isEqualTo(1);
        assertThat(byId2.get().getProducts().contains(product2)).isTrue();
    }

    @Test
    public void sendDeleteProductFromOrdersEvent_ShouldDeleteProductAndOrder_WhenOrderHasNoProducts() {
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order = OrderDataBuilder.buildOrderWithAllFields()
                .products(Set.of(product1))
                .build();

        productRepository.save(product1);
        orderRepository.save(order);

        testSender.sendDeleteProductFromOrdersEvent(product1.getId());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Product> byId = productRepository.findById(product1.getId());
            assertThat(byId).isNotPresent();
        });

        Optional<Order> byId = orderRepository.findById(order.getId());
        assertThat(byId).isNotPresent();
    }

    @Test
    public void sendDeleteProductFromOrdersEvent_ShouldDoNothing_WhenProductNotExists() {
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order1 = OrderDataBuilder.buildOrderWithAllFields()
                .products(Set.of(product1))
                .build();

        orderRepository.saveAll(List.of(order1));

        testSender.sendDeleteProductFromOrdersEvent(product1.getId());
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Product> byId = productRepository.findById(product1.getId());
            assertThat(byId).isNotPresent();
        });
    }

    @Test
    public void sendDeleteProductFromOrdersEvent_ShouldNotDeleteProduct_WhereStatusIsCompletedOrCancelled() {
        Product product1 = ProductDataBuilder.buildProductWithAllFields().build();
        Product product2 = ProductDataBuilder.buildProductWithAllFields().build();
        Order order1 = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.COMPLETED)
                .products(Set.of(product1, product2))
                .build();
        Order order2 = OrderDataBuilder.buildOrderWithAllFields()
                .status(OrderStatus.CANCELLED)
                .products(Set.of(product1, product2))
                .build();

        productRepository.save(product1);
        orderRepository.saveAll(List.of(order1, order2));

        testSender.sendDeleteProductFromOrdersEvent(product1.getId());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(testListener.hasReceived(product1.getId())).isTrue());

        Optional<Order> byId1 = orderRepository.findById(order1.getId());
        assertThat(byId1).isPresent();
        assertThat(byId1.get().getProducts()).isNotNull();
        assertThat(byId1.get().getProducts().size()).isEqualTo(2);
        assertThat(byId1.get().getProducts().contains(product2)).isTrue();

        Optional<Order> byId2 = orderRepository.findById(order2.getId());
        assertThat(byId2).isPresent();
        assertThat(byId2.get().getProducts()).isNotNull();
        assertThat(byId2.get().getProducts().size()).isEqualTo(2);
        assertThat(byId2.get().getProducts().contains(product2)).isTrue();
    }
}