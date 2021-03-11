package com.foodtech.back.manual;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.foodtech.back.entity.auth.Admin;
import com.foodtech.back.entity.auth.FirebaseToken;
import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.entity.model.iiko.DeliveryZone;
import com.foodtech.back.entity.model.iiko.Product;
import com.foodtech.back.repository.auth.AdminRepository;
import com.foodtech.back.repository.auth.FirebaseTokenRepository;
import com.foodtech.back.repository.iiko.IikoDeliveryZoneRepository;
import com.foodtech.back.repository.model.AddressDirectoryRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.repository.model.UserRepository;
import com.foodtech.back.service.iiko.IikoOrderStatusCheckExecutor;
import com.foodtech.back.service.iiko.IikoRequestService;
import com.foodtech.back.service.model.AddressDirectoryService;
import com.foodtech.back.service.model.DeliveryZoneService;
import com.foodtech.back.service.notification.push.FirebasePushSender;
import com.foodtech.back.service.payment.cloud.CloudCheck;
import com.foodtech.back.service.payment.cloud.CloudPaymentService;
import com.foodtech.back.util.mapper.CloudMapper;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
class ManualCodeExecution {

    @Autowired
    DeliveryZoneService deliveryZoneService;

    @Autowired
    IikoDeliveryZoneRepository deliveryZoneRepository;

    @Autowired
    private FirebasePushSender firebasePushSender;

    @Autowired
    AddressDirectoryRepository addressDirectoryRepository;

    @Autowired
    AddressDirectoryService addressDirectoryService;

    @Autowired
    IikoOrderStatusCheckExecutor statusCheckExecutor;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudPaymentService cloudPaymentService;

    @Autowired
    private FirebaseTokenRepository firebaseTokenRepository;

    @Test
    @Disabled
    void saveDeliveryZones() {
        deliveryZoneService.saveDeliveryZones();
    }

    @Test
    public void addAdmin() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Admin admin = new Admin();
        admin.setName("Erkin");
        admin.setCreated(LocalDateTime.now());
        admin.setRoles(Set.of(Role.ROLE_ADMIN));
        admin.setPassword(encoder.encode("iWxxjlYkYL"));
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
    @Transactional
    void jsonTest() throws JsonProcessingException {
        Optional<User> byCountryCodeAndMobileNumber = userRepository.findByCountryCodeAndMobileNumber("7", "328");
        System.out.println();
    }

    @Test
//    @Transactional
    public void test(){

        String text = "В  WOW KITCHEN обновилось меню, теперь в нем стало больше всего.\n" +
                "\n" +
                "Расширили зону доставки и теперь доставляем по всему левому берегу, даже в Акорду и Библиотеку";

        List<FirebaseToken> all = firebaseTokenRepository.findAll();

        all.forEach(firebaseToken -> {
            firebasePushSender.sendPushByToken(firebaseToken.getToken(), text);
        });
    }

    @Test
    public void customerToTxt()throws Exception {

        FileWriter myWriter = new FileWriter("customers_wow_02_2020.txt");
        List<User> all = userRepository.findAll();
        List<User> collect = all.stream()
                .sorted((o1, o2) -> (int) (o1.getId() - o2.getId()))
                .collect(Collectors.toList());

        for (User user : collect) {
            Address address = user.getAddresses().stream()
                    .filter(addr -> addr.isActual())
                    .findAny()
                    .get();
            myWriter.write(user.getName() + "  " + user.getMobileNumber() + " " + address.getStreet()+ " " + user.getCreated() + "\n");
        }
        myWriter.close();
    }

    public void tes() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");
        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Name");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Age");
        headerCell.setCellStyle(headerStyle);
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
