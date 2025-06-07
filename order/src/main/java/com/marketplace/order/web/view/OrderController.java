package com.marketplace.order.web.view;

import com.marketplace.order.mapper.SimpleOrderMapper;
import com.marketplace.order.service.OrderCrudService;
import com.marketplace.order.service.OrderSettingsService;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.mapper.SimpleProductMapper;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final ProductCrudService productCrudService;

    private final OrderCrudService orderCrudService;

    private final OrderSettingsService orderSettingsService;

    private final SimpleOrderMapper simpleOrderMapper;

    private final SimpleProductMapper simpleProductMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public String getAllOrders(Model model) {
        List<Order> orders = orderCrudService.findAll();
        model.addAttribute("orders", simpleOrderMapper.mapOrdersToOrderResponseDtos(orders));
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String getOrderById(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);

        model.addAttribute("isPayable", true);
        List<Product> products = order.getProductIds().stream()
                .map(productId -> {
                    Product product = productCrudService.getById(productId);

                    if (!product.getActive()) {
                        model.addAttribute("isPayable", false);
                    }

                    return product;
                })
                .toList();

        model.addAttribute("order", simpleOrderMapper.mapOrderToOrderResponseDto(order));
        model.addAttribute("products", simpleProductMapper.mapProductsToProductResponseDtos(products));

        return "order";
    }

    @GetMapping("/user-order")
    public String getUserOrder(
            Model model
    ) {
        Optional<Order> orderOptional = orderSettingsService.findOrderByOwnerIdAndStatus(OrderStatus.IN_PROGRESS);
        orderOptional.ifPresent(order -> {
            model.addAttribute("currentOrder", simpleOrderMapper.mapOrderToOrderResponseDto(order));

            model.addAttribute("isPayable", true);
            List<Product> products = order.getProductIds().stream()
                    .map(productId -> {
                        Product product = productCrudService.getById(productId);

                        if (!product.getActive()) {
                            model.addAttribute("isPayable", false);
                        }

                        return product;
                    })
                    .toList();
            model.addAttribute("orderProducts", simpleProductMapper.mapProductsToProductResponseDtos(products));

            BigDecimal totalSum = products.stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalSum", totalSum);

        });

        List<Order> ordersByOwnerIdAndStatusIn = orderSettingsService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        model.addAttribute("historyOrders", simpleOrderMapper.mapOrdersToOrderResponseDtos(ordersByOwnerIdAndStatusIn));

        return "user-order";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{orderId}/update")
    public String getUpdateOrder(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);
        model.addAttribute("order", simpleOrderMapper.mapOrderToOrderResponseDto(order));

        return "order-edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{orderId}/update")
    public String updateOrder(
            @PathVariable String orderId,
            @ModelAttribute OrderUpdateRequest orderUpdateRequest
    ) {
        orderCrudService.update(orderId, orderUpdateRequest);
        return "redirect:/orders/" + orderId;
    }

    @PutMapping("/add-product/{productId}")
    public String addProductToOrder(
            @PathVariable String productId,
            Model model
    ) {
        orderSettingsService.addProductToOrder(productId);
        return "redirect:/orders/" + getUserOrder(model);
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
        orderSettingsService.removeProductFromOrder(productId);
        return "home";
    }

    @PostMapping("/user-order/pay")
    public String payForOrder() {
        orderSettingsService.payForOrder();
        return "redirect:/orders/user-order";
    }
}
