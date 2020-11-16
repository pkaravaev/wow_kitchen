package com.foodtech.back.controller.admin;

import com.foodtech.back.entity.model.Order;
import com.foodtech.back.service.model.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/orders/page/{page}")
    public String orders(@PathVariable Integer page, Model model) {
        PageRequest pageRequest = PageRequest.of(page - 1, 15, Sort.by("id").descending());
        Page<Order> orderPage = orderService.getAllForAdmin(pageRequest);
        int totalPages = orderPage.getTotalPages();
        if(totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages).boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("currentPage", page);
        model.addAttribute("orders", orderPage.getContent());
        return "orders/orderList";
    }

    @GetMapping("/admin/orders/page/{page}/{orderId}/items")
    public String orderItems(@PathVariable Integer page, @PathVariable Long orderId, Model model) {
        model.addAttribute("currentPage", page);
        model.addAttribute("items", orderService.getItemsByOrderIdForAdmin(orderId));
        return "orders/items";
    }
}
