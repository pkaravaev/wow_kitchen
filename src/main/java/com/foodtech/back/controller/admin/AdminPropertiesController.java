package com.foodtech.back.controller.admin;

import com.foodtech.back.entity.util.AppProperty;
import com.foodtech.back.service.properties.PropertiesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminPropertiesController {

    private final PropertiesService service;

    public AdminPropertiesController(PropertiesService service) {
        this.service = service;
    }

    @GetMapping("/admin/properties")
    public String getProperties(Model model) {
        List<AppProperty> properties = service.getAll();
        model.addAttribute("properties", properties);
        return "properties/properties";
    }

    @PostMapping("/admin/properties")
    public String saveProperty(@ModelAttribute AppProperty property, RedirectAttributes redirectAttributes) {
        boolean saved = service.saveProperty(property.getId(), property.getValue());
        String message = saved ? "Сохранено" : "Произошла ошибка";
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/admin/properties";
    }
}
