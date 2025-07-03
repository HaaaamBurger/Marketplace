package com.marketplace.order.web.view;

import com.marketplace.order.mapper.OrderEntityMapper;
import com.marketplace.order.service.OrderCrudService;
import com.marketplace.order.service.OrderManagerService;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.mapper.ProductEntityMapper;
import com.marketplace.product.service.ProductValidationService;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class UserOrderController {

    private final OrderCrudService orderCrudService;

    private final OrderManagerService orderManagerService;

    private final ProductValidationService productValidationService;

    private final OrderEntityMapper orderEntityMapper;

    private final ProductEntityMapper productEntityMapper;

    @GetMapping("/{orderId}")
    public String getOrderById(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);

        Set<Product> products = order.getProducts();
        model.addAttribute("isPayable", !productValidationService.validateProducts(products));
        model.addAttribute("products", productEntityMapper.mapProductsToProductResponseDtos(products));
        model.addAttribute("order", orderEntityMapper.mapOrderToOrderResponseDto(order));

        return "order";
    }

    @GetMapping("/user-order")
    public String getUserOrder(
            Model model
    ) {
        Optional<Order> orderByOwnerIdAndStatus = orderManagerService.findOrderByOwnerIdAndStatus(OrderStatus.IN_PROGRESS);

        orderByOwnerIdAndStatus.ifPresent(order -> {
            Set<Product> products = order.getProducts();
            model.addAttribute("isPayable", !productValidationService.validateProducts(products));
            model.addAttribute("orderProducts", productEntityMapper.mapProductsToProductResponseDtos(products));
            model.addAttribute("totalSum", orderManagerService.calculateTotalSum(products));
            model.addAttribute("currentOrder", orderEntityMapper.mapOrderToOrderResponseDto(order));
        });

        List<Order> ordersByOwnerIdAndStatusIn = orderManagerService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        model.addAttribute("historyOrders", orderEntityMapper.mapOrdersToOrderResponseDtos(ordersByOwnerIdAndStatusIn));

        return "user-order";
    }

    @PutMapping("/add-product/{productId}")
    public String addProductToOrder(
            @PathVariable String productId
    ) {
        orderManagerService.addProductToOrder(productId);
        return "redirect:/orders/user-order";
    }

    @DeleteMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable String orderId) {
        orderCrudService.delete(orderId);
        return "redirect:/home";
    }

    @DeleteMapping("/remove-product/{productId}")
    public String removeProductFromOrder(
            @PathVariable String productId
    ) {
        orderManagerService.removeProductFromOrder(productId);
        return "redirect:/home";
    }

    @PostMapping("/user-order/pay")
    public String payForOrder() {
        orderManagerService.payForOrder();
        return "redirect:/orders/user-order";
    }
}
