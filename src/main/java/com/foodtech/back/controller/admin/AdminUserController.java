package com.foodtech.back.controller.admin;

import com.foodtech.back.entity.model.PreferencesKeyWord;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.UserPreferences;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.model.AddressService;
import com.foodtech.back.service.model.BankCardService;
import com.foodtech.back.service.model.UserPreferencesService;
import com.foodtech.back.service.model.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final UserService userService;

    private final AddressService addressService;

    private final BankCardService bankCardService;

    private final BonusService bonusService;

    private final UserPreferencesService userPreferencesService;

    public AdminUserController(UserService userService, AddressService addressService, BankCardService bankCardService,
                               BonusService bonusService, UserPreferencesService userPreferencesService) {
        this.userService = userService;
        this.addressService = addressService;
        this.bankCardService = bankCardService;
        this.bonusService = bonusService;
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping("/admin/users")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "users/userList";
    }

    @GetMapping("/admin/users/{userId}/addresses")
    public String getAddresses(@PathVariable Long userId, Model model) {
        model.addAttribute("addresses", addressService.getAllByUser(userId));
        return "users/addresses";
    }

    @GetMapping("/admin/users/{userId}/cards")
    public String getCards(@PathVariable Long userId, Model model) {
        model.addAttribute("cards", bankCardService.getCards(userId));
        return "users/cards";
    }

    @GetMapping("/admin/users/{userId}/preferences")
    public String getPreferences(@PathVariable Long userId, Model model) {
        UserPreferences preferences = userPreferencesService.findByUserId(userId).orElseThrow();
        model.addAttribute("preferences", preferences);
        return "users/preferences";
    }

    @GetMapping("/admin/users/{userId}/bonuses")
    public String getBonusChangeForm(@PathVariable Long userId, Model model) {
        User user = userService.get(userId).orElseThrow();
        model.addAttribute("bonusAmount", user.getBonusAccount().getBonusAmount());
        model.addAttribute("userId", userId);
        return "users/bonuses";
    }

    @PostMapping("/admin/users/{userId}/bonuses")
    public String changeBonuses(@PathVariable Long userId, @RequestParam Integer newValue, RedirectAttributes redirectAttributes) {
        bonusService.changeBonusAmount(userId, newValue);
        redirectAttributes.addFlashAttribute("message", "Изменено");
        return "redirect:/admin/users/" + userId + "/bonuses";
    }

    @GetMapping("/admin/preferences/words")
    public String getPreferencesKeyWord(Model model) {
        model.addAttribute("words", userPreferencesService.getAllKeyWordsForAdmin());
        model.addAttribute("newWord", new PreferencesKeyWord());
        return "preferences/words";
    }

    @PostMapping("/admin/preferences/words")
    public String addKeyWord(@ModelAttribute PreferencesKeyWord newWord) {
        userPreferencesService.addKeyWord(newWord);
        return "redirect:/admin/preferences/words";
    }

    @PostMapping("/admin/users/preferences/words/{id}")
    public String deleteKeyWord(@PathVariable Integer id) {
        userPreferencesService.deleteKeyWord(id);
        return "redirect:/admin/preferences/words";
    }
}
