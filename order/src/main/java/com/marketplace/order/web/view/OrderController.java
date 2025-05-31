package com.marketplace.order.web.view;

import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.mapper.OrderEntityMapper;
import com.marketplace.product.mapper.ProductEntityMapper;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.web.model.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// TODO reorganize all endpoints' name (add delete, update..., /products/{id}/orders/{id}...)
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    private final ProductService productService;

    private final OrderEntityMapper orderEntityMapper;

    private final ProductEntityMapper productEntityMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orderEntityMapper.mapOrdersToOrderResponseDtos(orders));
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String getOrderById(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderService.findById(orderId);
        List<Product> products = order.getProductIds().stream()
                .map(productService::findById)
                .toList();

        model.addAttribute("order", orderEntityMapper.mapOrderToOrderResponseDto(order));
        model.addAttribute("products", productEntityMapper.mapProductsToProductResponseDtos(products));

        return "order";
    }

    @GetMapping("/my-order")
    public String getAuthUserOrder(
            Model model
    ) {
        Optional<Order> orderOptional = orderService.findByOwnerId();
        orderOptional.ifPresent(order -> {
            model.addAttribute("currentOrder", order);

            List<Product> products = order.getProductIds().stream()
                    .map(productService::findById)
                    .toList();
            model.addAttribute("orderProducts", productEntityMapper.mapProductsToProductResponseDtos(products));

            BigDecimal totalSum = products.stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalSum", totalSum);

            model.addAttribute("previousOrders", List.of());
        });

        return "my-order";
    }

    // TODO maybe it's better to make it /orders/{orderId}/products/{productId}
    @PutMapping("/products/{productId}")
    public String addProductToOrder(
            @PathVariable String productId,
            Model model
    ) {
        orderService.addProductToOrder(productId);
        return getAuthUserOrder(model);
    }

    @DeleteMapping("/{orderId}")
    public String deleteOrder(@PathVariable String orderId) {
        orderService.delete(orderId);
        return "redirect:/home";
    }

    @DeleteMapping("/remove-product/{productId}")
    public String removeProductFromOrder(
            @PathVariable String productId
    ) {
        orderService.removeProductFromOrder(productId);
        return "home";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/update/{orderId}")
    public String getUpdateOrder(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderService.findById(orderId);
        model.addAttribute("order", order);

        return "order-edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/update/{orderId}")
    public String updateOrder(
            @PathVariable String orderId,
            @Valid @ModelAttribute OrderUpdateRequest orderUpdateRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "order-edit";
        }

        orderService.update(orderId, orderUpdateRequest);
        return "redirect:/orders/" + orderId;
    }
}
