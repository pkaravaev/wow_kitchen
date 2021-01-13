package com.foodtech.back.manual;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.foodtech.back.dto.iiko.IikoOrderRequest;
import com.foodtech.back.entity.auth.Admin;
import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.repository.auth.AdminRepository;
import com.foodtech.back.repository.model.AddressDirectoryRepository;
import com.foodtech.back.security.AdminDetailsService;
import com.foodtech.back.service.iiko.IikoOrderStatusCheckExecutor;
import com.foodtech.back.service.model.AddressDirectoryService;
import com.foodtech.back.service.model.DeliveryZoneService;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringJUnitWebConfig
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(value = {
        "classpath:sms.properties",
        "classpath:crypt.properties",
        "classpath:iiko.properties",
        "classpath:delivery.properties",
        "classpath:push.properties",
        "classpath:payment.properties",
        "classpath:client_messages.properties",
        "classpath:rabbitmq.properties",
        "classpath:payment-dev.properties"
})
@Disabled
class ManualCodeExecution {

    @Autowired
    DeliveryZoneService deliveryZoneService;

    @Autowired
    AddressDirectoryRepository addressDirectoryRepository;

    @Autowired
    AddressDirectoryService addressDirectoryService;

    @Autowired
    IikoOrderStatusCheckExecutor statusCheckExecutor;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    @Disabled
    void saveDeliveryZones() {
        deliveryZoneService.saveDeliveryZones();
    }

    @Test
    public void addAdmin() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Admin admin = new Admin();
        admin.setName("Rizvan");
        admin.setCreated(LocalDateTime.now());
        admin.setRoles(Set.of(Role.ROLE_ADMIN));
        admin.setPassword(encoder.encode("rizvan787"));
        adminRepository.save(admin);
    }

    @Test
    @Disabled
    void printAddressDirectory() {
        List<AddressDirectory> all = addressDirectoryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        all.forEach(ad -> sb.append("'Нур-Султан ").append(ad.getStreet()).append(" ").append(ad.getHouse()).append("', "));
        sb.append("]");
        System.out.println(sb);
    }

    @Test
    @Disabled
    void checkDirectoryCoordinates() {
        List<AddressDirectory> all = addressDirectoryRepository.findAll();
        for (AddressDirectory directory : all) {
            if (deliveryZoneService.getDeliveryTerminalByCoordinatesInActiveZone(DeliveryCoordinate.of(directory.getLatitude(), directory.getLongitude())).isEmpty()) {
                System.out.println("No terminal found for: " + directory.getStreet() + " " + directory.getHouse());
            }
        }
    }

    @Test
    @Disabled
    void addDeliveryZoneToAddressDirectory() {
        List<AddressDirectory> all = addressDirectoryRepository.findAll();
        for (AddressDirectory address : all) {
            Optional<DeliveryZone> zone = deliveryZoneService.getByCoordinates(DeliveryCoordinate.of(address.getLatitude(), address.getLongitude()));
            if (zone.isEmpty()) {
                continue;
            }
            address.setDeliveryZone(zone.get());
        }
        addressDirectoryRepository.saveAll(all);
    }

    @Test
    @Disabled
    void testStatusChecker() {
        statusCheckExecutor.checkStatuses();
    }

    @Test
    @Disabled
    void jsonTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        IikoOrderRequest request = IikoOrderRequest.form(order());
        String s = objectWriter.writeValueAsString(request);
        System.out.println(s);
    }

    private static Order order() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalCost(100);
        order.setUser(new User());
        Address address = new Address();
        address.setHome("1A");
        order.setAddress(address);
        KitchenDeliveryTerminal terminal = new KitchenDeliveryTerminal();
        address.setDeliveryTerminal(terminal);
        Kitchen kitchen = new Kitchen();
        kitchen.setPaymentTypeExternalRevision(2222L);
        terminal.setKitchen(kitchen);

        OrderItem item = new OrderItem();
        item.setAmount(1);
        order.setItems(List.of(item));
        Product product = new Product();
        product.setPrice(100);
        item.setProduct(product);

        return order;
    }


}
