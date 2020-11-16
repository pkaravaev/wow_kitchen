package com.foodtech.back.controller.admin;

import com.foodtech.back.service.model.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.foodtech.back.service.model.CacheService.*;

@Controller
@RequestMapping("/admin/caches")
@Slf4j
public class AdminCacheController {

    private final CacheService cacheService;

    public AdminCacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping
    public String caches() {
        return "caches";
    }

    @GetMapping(path = "{cacheName}")
    public String flushCache(@PathVariable String cacheName, RedirectAttributes redirectAttributes) {
        boolean flushed = flushCache(cacheName);
        String message = flushed ? "Кэш очищен" : "Ошибка";
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/admin/caches";
    }

    private boolean flushCache(String cacheName) {
        switch (cacheName) {
            case LOGGED_USER_CACHE:
                cacheService.flushLoggedUserCache();
                return true;
            case PRODUCTS_FOR_FRONT_APP:
                cacheService.flushProductsForFrontAppCache();
                return true;
            case DELIVERY_ZONE:
                cacheService.flushDeliveryZoneCache();
                return true;
            case ADDRESSES_DIRECTORY:
                cacheService.flushAddressDirectoryCache();
                return true;
            default:
                log.error("Cache with name '{}' not found", cacheName);
                return false;
        }
    }
}
