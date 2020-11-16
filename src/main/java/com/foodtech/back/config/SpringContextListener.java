package com.foodtech.back.config;

import com.foodtech.back.dto.model.AddressDirectoryDto;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.service.model.AddressDirectoryService;
import com.foodtech.back.service.model.DeliveryZoneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SpringContextListener {

    private final DeliveryZoneService deliveryZoneService;

    private final AddressDirectoryService addressDirectoryService;

    public SpringContextListener(DeliveryZoneService deliveryZoneService, AddressDirectoryService addressDirectoryService) {
        this.deliveryZoneService = deliveryZoneService;
        this.addressDirectoryService = addressDirectoryService;
    }

    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent contextRefreshedEvent) {
//      https://stackoverflow.com/questions/34443617/how-to-swich-on-contextloadlistener-depends-on-spring-profile
        Environment environment = contextRefreshedEvent.getApplicationContext().getEnvironment();
        if (environment.acceptsProfiles(Profiles.of("test", "unit"))) {
            return;
        }
        initCache();
    }

    private void initCache() {
        List<AddressDirectoryDto> directory = addressDirectoryService.getAllInActiveZones();
        log.info("Address directory loaded in cache - ({}) items", directory.size());

        List<DeliveryZone> deliveryZones = deliveryZoneService.getDeliveryZones();
        deliveryZones.forEach(z -> log.info("Delivery zone '{}' loaded to cache", z.getName()));
    }
}
