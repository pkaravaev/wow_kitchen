package com.foodtech.back.controller.admin;

import com.foodtech.back.entity.bonus.PromoCodeImpersonal;
import com.foodtech.back.service.bonus.BonusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminImpersonalPromoCodeController {

    private final BonusService bonusService;

    public AdminImpersonalPromoCodeController(BonusService bonusService) {
        this.bonusService = bonusService;
    }

    @GetMapping(path = "/admin/bonuses/promoCodes")
    public String getPromoCodes(Model model) {
        model.addAttribute("newPromoCode", new PromoCodeImpersonal());
        model.addAttribute("promoCodes", bonusService.getImpersonalPromoCodes());
        return "bonuses/promoCodes";
    }

    @PostMapping(path = "/admin/bonuses/promoCode")
    public String addPromoCode(@ModelAttribute PromoCodeImpersonal newPromoCode, RedirectAttributes redirectAttributes) {
        boolean added = bonusService.addImpersonalPromoCode(newPromoCode);
        redirectAttributes.addFlashAttribute("message", added ? "Промо-код добавлен": "Произошла ошибка");
        return "redirect:/admin/bonuses/promoCodes";
    }

    @PostMapping(path = "/admin/bonuses/promoCodes/delete/{id}")
    public String addPromoCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bonusService.deleteImpersonalPromoCode(id);
        redirectAttributes.addFlashAttribute("message", "Удалено");
        return "redirect:/admin/bonuses/promoCodes";
    }
}
