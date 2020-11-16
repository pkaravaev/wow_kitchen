package com.foodtech.back.service.notification.ampq;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.repository.payment.BankCardBindRequestRepository;
import com.foodtech.back.util.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RabbitMqService {

    private final RabbitAdmin rabbitAdmin;

    private final RabbitTemplate rabbitTemplate;

    private final OrderRepository orderRepository;

    private final BankCardBindRequestRepository bindRequestRepository;

    private final ResourcesProperties properties;

    public RabbitMqService(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate, OrderRepository orderRepository,
                           BankCardBindRequestRepository bindRequestRepository, ResourcesProperties properties) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.orderRepository = orderRepository;
        this.bindRequestRepository = bindRequestRepository;
        this.properties = properties;
    }

    public String createQueue(RabbitMqQueueType type, String id) {
        String queuePrefix;
        switch (type) {
            case PAYMENT_QUEUE:
                queuePrefix = properties.getRabbitMqPaymentQueuePrefix();
                break;
            case CARD_BINDING_QUEUE:
                queuePrefix = properties.getRabbitMqCardBindingQueuePrefix();
                break;
            case ORDER_STATUS_QUEUE:
                queuePrefix = properties.getRabbitMqOrderStatusQueuePrefix();
                break;
            default:
                return null;
        }
        String queueName = queuePrefix + DigestUtils.md5Hex(id);

        try {
            rabbitAdmin.declareQueue(new Queue(queueName));
        } catch (Exception ex) {
            log.error(">>>>> Exception during RabbitMq '{}' queue declaration. Cause: '{}'", type.name(), ex.getMessage());
            return null;
        }

        return queueName;
    }

    public void sendMessage(String queueName, String message) {
        try {
            rabbitTemplate.convertAndSend(queueName, message);
            log.info("Message '{}' sent to amqp queue '{}'", message, queueName);
        } catch (Exception ex) {
            log.error(">>>>> Exception during RabbitMq message '{}' sending. Cause: '{}'", message, ex.getMessage());
        }
    }

    @Transactional
    public void deleteOldQueues(long queueLifeTime) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(queueLifeTime);
        List<String> orderQueues = orderRepository.findQueueNamesForDelete(beforeDate);
        List<String> bindQueues = bindRequestRepository.findQueueNamesForDelete(beforeDate);
        orderQueues.addAll(bindQueues);
        if (!orderQueues.isEmpty()) {
            log.info(">>>> Deleting old rabbit queues");
            try {
                orderQueues.forEach(rabbitAdmin::deleteQueue);
            } catch (Exception ex) {
                log.error(">>>>> Exception during deleting old RabbitMq queues. Cause: '{}'", ex.getMessage());
                return;
            }
            orderRepository.setNullToDeletedQueues(orderQueues);
            bindRequestRepository.setNullToDeletedQueues(orderQueues);
            log.info(">>>> Deleted {} queues", orderQueues.size());
        }
    }

    void sendTestMessage() {
        rabbitTemplate.convertAndSend("app-test88", ResponseCode.OK.toString());
    }
}
