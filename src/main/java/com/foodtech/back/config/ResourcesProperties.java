package com.foodtech.back.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class ResourcesProperties {

    /**
     * SMS
     */
    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.url.scheme}")
    private String smsUrlScheme;

    @Value("${sms.url.path}")
    private String smsUrlPath;

    @Value("${sms.url.host}")
    private String smsUrlHost;

    @Value("${sms.login}")
    private String smsLogin;

    @Value("${sms.password}")
    private String smsPassword;

    @Value("${sms.sender}")
    private String smsSender;

    @Value("${sms.connection.request.method}")
    private String smsMethod;

    @Value("${sms.charset}")
    private String smsCharset;

    @Value("${sms.footer}")
    private String smsFooter;

    @Value("${sms.auth.code.description}")
    private String smsCodeDescription;

    /**
     * Crypto
     */

    @Value("${crypto.symmetric-key}")
    private String symmetricKey;

    @Value("${crypto.algorithm}")
    private String algorithm;

    @Value("${crypto.secret}")
    private String secret;

    /**
     * Payment
     */

    @Value("${payment.cloud.public-id}")
    private String cloudPublicId;

    @Value("${payment.cloud.api-key}")
    private String cloudApiKey;

    @Value("${payment.cloud.url.test}")
    private String cloudTestUrl;

    @Value("${payment.cloud.url.base}")
    private String cloudBaseUrl;

    @Value("${payment.cloud.path.auth.crypt}")
    private String cloudAuthCryptPath;

    @Value("${payment.cloud.path.auth.token}")
    private String cloudAuthTokenPath;

    @Value("${payment.cloud.path.pares}")
    private String cloudPaResPath;

    @Value("${payment.cloud.path.cancel}")
    private String cloudCancelPath;

    @Value("${payment.cloud.path.confirm}")
    private String cloudConfirmPath;

    @Value("${payment.cloud.card.bind.3ds.callback.url}")
    private String cardBinding3DSCallbackUrl;

    @Value("${payment.cloud.3ds.callback.url}")
    private String payment3DSCallbackUrl;

    @Value("${payment.cloud.properties.test}")
    private String paymentPropertiesTest;

    @Value("${payment.currency}")
    private String paymentCurrency;

    @Value("${payment.card.bind.client.description}")
    private String cardBindingDescription;

    @Value("${payment.client.description}")
    private String paymentDescription;

    @Value("${payment.card.bind.amount}")
    private Integer paymentCardBindingAmount;
    /**
      *Iiko
     */

    @Value("${iiko.base.url}")
    private String iikoBaseUrl;

    @Value("${iiko.organization.id}")
    private String iikoOrganizationId;

    @Value("${iiko.user.id}")
    private String iikoUserId;

    @Value("${iiko.user.secret}")
    private String iikoUserSecret;

    /*
     * Delivery
     */
    @Value("${delivery.time.default}")
    private Integer defaultDeliveryTime;


    /*
     * Push
     */

    @Value("${push.message.title.default}")
    private String pushTitle;

    @Value("${push.message.order.sent.success}")
    private String orderSentSuccessPush;

    @Value("${push.message.order.sent.failed}")
    private String orderSendingFailedPush;

    @Value("${push.message.order.sent.collision}")
    private String orderSentBonusCollisionPush;

    @Value("${push.message.order.status.in-progress}")
    private String orderInProgressPush;

    @Value("${push.message.order.status.on-the-way}")
    private String orderOnTheWayPush;

    @Value("${push.message.order.status.delivered}")
    private String orderDeliveredPush;

    @Value("${push.message.order.status.cancelled}")
    private String orderCancelledPush;

    /*
    * Client messages
    * */

    @Value("${client.messages.error.default}")
    private String defaultErrorMsg;

    @Value("${client.messages.mobile.invalid}")
    private String mobNumberInvalidMsg;

    @Value("${client.messages.sms.sending.fail}")
    private String smsSendingFailedMsg;

    @Value("${client.messages.sms.sending.fail.delay}")
    private String smsSendingDelayMsg;

    @Value("${client.messages.sms.check.fail.invalid}")
    private String smsCodeInvalidMsg;

    @Value("${client.messages.sms.check.fail.sms.not.found}")
    private String smsCodeNotFoundMsg;

    @Value("${client.messages.sms.check.fail.max.attempt}")
    private String smsCheckMaxAttemptMsg;

    @Value("${client.messages.sms.check.fail.code.expired}")
    private String smsCodeExpiredMsg;

    @Value("${client.messages.auth.token.expired}")
    private String authTokenExpiredMsg;

    @Value("${client.messages.auth.refresh.token.invalid}")
    private String authRefreshTokenInvalidMsg;

    @Value("${client.messages.order.cart.invalid}")
    private String cartInvalidMsg;

    @Value("${client.messages.order.sending.fail}")
    private String orderSendingFailedMsg;

    @Value("${client.messages.order.not.found}")
    private String orderNotFoundMsg;

    @Value("${client.messages.address.invalid}")
    private String addressInvalidMsg;

    @Value("${client.messages.address.coordinates.invalid}")
    private String coordinatesInvalidMsg;

    @Value("${client.messages.payment.request.sending.fail}")
    private String paymentRequestSendingFailedMsg;

    @Value("${client.messages.payment.card.binding.fail}")
    private String cardBindingFailedMsg;

    @Value("${client.messages.payment.fail}")
    private String paymentFailedMsg;

    @Value("${client.message.promo.code.used}")
    private String promoCodeUsedMsg;

    @Value("${client.message.promo.code.not.found}")
    private String promoCodeNotFoundMsg;

    @Value("${client.message.promo.code.own}")
    private String ownPromoCodeMsg;

    @Value("${client.message.promo.code.exists}")
    private String promoCodeExistsMsg;

    @Value("${client.message.kitchen.closed}")
    private String kitchenClosedMsg;

    @Value("${client.message.delivery.zone.not.active}")
    private String deliveryZoneNotActiveMsg;


    /*
    * RabbitMq
    * */

    @Value("${rabbitmq.host}")
    private String rabbitMqHost;

    @Value("${rabbitmq.queue.prefix.payment}")
    private String rabbitMqPaymentQueuePrefix;

    @Value("${rabbitmq.queue.prefix.card.binding}")
    private String rabbitMqCardBindingQueuePrefix;

    @Value("${rabbitmq.queue.prefix.order.status}")
    private String rabbitMqOrderStatusQueuePrefix;

    @Value("${rabbitmq.message.card.binding.success}")
    private String cardBindingSuccessRabbitMqMsg;

    @Value("${rabbitmq.message.card.binding.failed.bad.request}")
    private String cardBindingFailedRabbitMqMsg;

    @Value("${rabbitmq.message.payment.success}")
    private String paymentSuccessRabbitMqMsg;

    @Value("${rabbitmq.message.payment.failed.bad.request}")
    private String paymentFailedRabbitMqMsg;

    @Value("${rabbitmq.message.order.send.failed}")
    private String orderSendingFailedRabbitMqMsg;

    @Value("${rabbitmq.message.order.delivered}")
    private String orderDeliveredRabbitMqMsg;

    @Value("${rabbitmq.message.order.cancelled}")
    private String orderCancelledRabbitMqMsg;
}
