package com.marketplace.order.web.view;

import com.marketplace.order.service.OrderCrudService;
import com.marketplace.order.service.OrderSettingsService;
import com.marketplace.order.web.dto.OrderResponse;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.mapper.OrderEntityMapper;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.product.mapper.ProductEntityMapper;
import com.marketplace.product.service.ProductCrudService;
import com.marketplace.product.web.model.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    private final OrderEntityMapper orderEntityMapper;

    private final ProductEntityMapper productEntityMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public String getAllOrders(Model model) {
        List<Order> orders = orderCrudService.findAll();
        model.addAttribute("orders", orderEntityMapper.mapOrdersToOrderResponseDtos(orders));
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String getOrderById(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);
        List<Product> products = productCrudService.findAll();

        model.addAttribute("order", orderEntityMapper.mapOrderToOrderResponseDto(order));
        model.addAttribute("products", productEntityMapper.mapProductsToProductResponseDtos(products));

        return "order";
    }

    @GetMapping("/user-order")
    public String getUserOrder(
            Model model
    ) {

        Optional<Order> orderOptional = orderSettingsService.findOrderByOwnerIdAndStatus(OrderStatus.IN_PROGRESS);
        orderOptional.ifPresent(order -> {
            model.addAttribute("currentOrder", orderEntityMapper.mapOrderToOrderResponseDto(order));

            List<Product> products = order.getProductIds().stream()
                    .map(productCrudService::getById)
                    .toList();
            model.addAttribute("orderProducts", productEntityMapper.mapProductsToProductResponseDtos(products));

            BigDecimal totalSum = products.stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalSum", totalSum);

        });

        List<Order> ordersByOwnerIdAndStatusIn = orderSettingsService.findOrdersByOwnerIdAndStatusIn(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        System.out.println(ordersByOwnerIdAndStatusIn);
        model.addAttribute("historyOrders", orderEntityMapper.mapOrdersToOrderResponseDtos(ordersByOwnerIdAndStatusIn));

        return "user-order";
    }

    @PutMapping("/{productId}/add-product")
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{orderId}/update")
    public String getUpdateOrder(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);
        model.addAttribute("order", order);

        return "order-edit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{orderId}/update")
    public String updateOrder(
            @PathVariable String orderId,
            @Valid @ModelAttribute OrderUpdateRequest orderUpdateRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "order-edit";
        }

        orderCrudService.update(orderId, orderUpdateRequest);
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/user-order/pay")
    public String payForOrder() {
        orderSettingsService.payForOrder();
        return "redirect:/orders/user-order";
    }
}
