package com.foodtech.back.service.notification.ampq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Profile({"prod", "dev"})
public class RabbitMqScheduleService {

    private static final long QUEUE_LIFETIME = 1L;
    private static final long QUEUE_CHECK_DELAY = 86_400_000L; /* 1 сутки */

    private final RabbitMqService rabbitMqService;

    public RabbitMqScheduleService(RabbitMqService rabbitMqService) {
        this.rabbitMqService = rabbitMqService;
    }

    @Transactional
    @Scheduled(fixedDelay = QUEUE_CHECK_DELAY)
    public void autoDeleteOldQueues() {
        log.info("Auto deleting old RabbitMq queues");
        rabbitMqService.deleteOldQueues(QUEUE_LIFETIME);
    }

    @Scheduled(fixedDelay = 300_000)
    public void sendTestMessage() {
        try {
            rabbitMqService.sendTestMessage();
        } catch (Exception ex) {
            log.error(">>>>> Exception during sending test message to rabbit mq. Cause: '{}'", ex.getMessage());
        }
    }
}
