package com.foodtech.back.unit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("unit")
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
public class AbstractUnitTest {

}
