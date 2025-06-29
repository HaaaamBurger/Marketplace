package com.marketplace.order.web.view;

import com.marketplace.order.mapper.OrderEntityMapper;
import com.marketplace.order.service.OrderCrudService;
import com.marketplace.order.web.dto.OrderUpdateRequest;
import com.marketplace.order.web.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminOrderController {

    private final OrderCrudService orderCrudService;

    private final OrderEntityMapper orderEntityMapper;

    @GetMapping("/all")
    public String getAllOrders(Model model) {
        List<Order> orders = orderCrudService.findAll();
        model.addAttribute("orders", orderEntityMapper.mapOrdersToOrderResponseDtos(orders));
        return "orders";
    }

    @GetMapping("/{orderId}/update")
    public String getUpdateOrder(
            Model model,
            @PathVariable String orderId
    ) {
        Order order = orderCrudService.findById(orderId);
        model.addAttribute("order", orderEntityMapper.mapOrderToOrderResponseDto(order));

        return "order-edit";
    }

    @PutMapping("/{orderId}/update")
    public String updateOrder(
            @PathVariable String orderId,
            @ModelAttribute OrderUpdateRequest orderUpdateRequest
    ) {
        orderCrudService.update(orderId, orderUpdateRequest);
        return "redirect:/orders/" + orderId;
    }

}
