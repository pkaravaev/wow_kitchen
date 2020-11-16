package com.foodtech.back.service.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Profile({"prod", "dev"})
public class ProductScheduleService {

    private final static String MENU_UPDATING_CRON_EXP = "0 0 9,12 * * *"; /* Каждый день в 9 и 12 часов по Астане */
    private final static long FIVE_MINUTES = 300_000;

    private final ProductService productService;

    public ProductScheduleService(ProductService productService) {
        this.productService = productService;
    }

    @Transactional
    @Scheduled(cron = MENU_UPDATING_CRON_EXP)
    public void autoUpdateNomenclature() {
        log.info("Auto menu updating");
        productService.saveNomenclature();
    }

    @Transactional
    @Scheduled(fixedDelay = FIVE_MINUTES)
    public void updateStopList() {
        log.debug("Auto stop list updating");
        productService.updateStopList();
    }
}
