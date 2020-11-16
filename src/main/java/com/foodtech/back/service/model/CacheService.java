package com.foodtech.back.service.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

    public static final String LOGGED_USER_CACHE = "loggedUser";
    public static final String PRODUCTS_FOR_FRONT_APP = "productsForFrontApp";
    public static final String DELIVERY_ZONE = "deliveryZone";
    public static final String ADDRESSES_DIRECTORY = "addresses";

    @CacheEvict(value = LOGGED_USER_CACHE, allEntries = true)
    public void flushLoggedUserCache() {
        log.info("Logged user cache flushed");
    }

    @CacheEvict(value = PRODUCTS_FOR_FRONT_APP, allEntries = true)
    public void flushProductsForFrontAppCache() {
        log.info("Products for front app cache flushed");
    }

    @CacheEvict(value = DELIVERY_ZONE, allEntries = true)
    public void flushDeliveryZoneCache() {
        log.info("Delivery zone cache flushed");
    }

    @CacheEvict(value = ADDRESSES_DIRECTORY, allEntries = true)
    public void flushAddressDirectoryCache() {
        log.info("Address directory cache flushed");
    }

}
