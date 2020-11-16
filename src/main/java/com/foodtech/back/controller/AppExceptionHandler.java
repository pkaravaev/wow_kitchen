package com.foodtech.back.controller;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.security.JwtUser;
import com.foodtech.back.util.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

import static com.foodtech.back.util.ControllerUtil.errorResponse;
import static com.foodtech.back.util.ResponseCode.*;
import static java.util.Objects.nonNull;

@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class AppExceptionHandler {

    private final ResourcesProperties properties;

    public AppExceptionHandler(ResourcesProperties properties) {
        this.properties = properties;
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResponse handleValidationException(MethodArgumentNotValidException ex, @AuthenticationPrincipal JwtUser user) {
        log.error("Request invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(REQUEST_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ConstraintViolationException.class)
    public JsonResponse handleValidationException(ConstraintViolationException ex, @AuthenticationPrincipal JwtUser user) {
        log.error("Request invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(REQUEST_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public JsonResponse dataIntegrityViolationException(DataIntegrityViolationException ex, @AuthenticationPrincipal JwtUser user) {
        log.error("Request invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(REQUEST_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoSuchElementException.class)
    public JsonResponse handleNoElementException(NoSuchElementException ex, @AuthenticationPrincipal JwtUser user) {
        log.info("Request invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(REQUEST_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResponse handleIllegalArgumentException(IllegalArgumentException ex, @AuthenticationPrincipal JwtUser user) {
        log.info("Request invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(REQUEST_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MobileNumberInvalidException.class)
    public JsonResponse handleMobilNumberInvalidException(MobileNumberInvalidException ex) {
        log.info("Request failed. Mobile number '{}' invalid", ex.getUserNumber());
        return errorResponse(MOB_NUM_INVALID, properties.getMobNumberInvalidMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SmsSendingNotAllowedException.class)
    public JsonResponse handleSmsSendingNotAllowedException(SmsSendingNotAllowedException ex) {
        log.info("Sms sending to '{}' failed - delay time", ex.getUserNumber());
        return errorResponse(SMS_DELAY, properties.getSmsSendingDelayMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SmsSendingFailedException.class)
    public JsonResponse handleSmsSendingFailedException(SmsSendingFailedException ex) {
        log.info("Sms sending to '{}' failed - http sending error", ex.getUserNumber());
        return errorResponse(SMS_SENDING_FAIL, properties.getSmsSendingFailedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SmsNotFoundException.class)
    public JsonResponse handleSmsNotFoundException(SmsNotFoundException ex) {
        log.info("Sms checking for '{}' failed - sms not found", ex.getUserNumber());
        return errorResponse(SMS_NOT_FOUND, properties.getSmsCodeNotFoundMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SmsCheckFailedException.class)
    public JsonResponse handleSmsCheckFailedException(SmsCheckFailedException ex) {
        log.info("Sms checking failed. Mobile: '{}'. Cause: '{}'", ex.getUserNumber(), ex.getReason());
        return errorResponse(ex.getReason(), ex.getUserMessage());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAddressInvalidException.class)
    public JsonResponse handleUserAddressInvalidException(UserAddressInvalidException ex) {
        log.info("User address checking for '{}' failed - address invalid", ex.getUserNumber());
        return errorResponse(USER_ADDRESS_INVALID, properties.getAddressInvalidMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TokenRefreshDataInvalidException.class)
    public JsonResponse handleRefreshTokenDataInvalidException(TokenRefreshDataInvalidException ex) {
        log.info("Refreshing token for '{}' failed.", ex.getUserNumber());
        return errorResponse(REFRESH_TOKEN_DATA_INVALID, properties.getDefaultErrorMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CartInvalidException.class)
    public JsonResponse handleCartInvalidException(CartInvalidException ex, @AuthenticationPrincipal JwtUser user) {
        log.error("Cart invalid. Cause: '{}'. User: '{}'", ex.getMessage(), logUser(user));
        return errorResponse(CART_INVALID, properties.getCartInvalidMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PromoCodeNotFoundException.class)
    public JsonResponse handlePromoCodeNotFoundException(@AuthenticationPrincipal JwtUser user) {
        log.info("Promo code applying for failed. Cause: 'code not found'. User: '{}'", logUser(user));
        return errorResponse(PROMO_CODE_NOT_FOUND, properties.getPromoCodeNotFoundMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RegistrationPromoCodeAlreadyUsedException.class)
    public JsonResponse handlePromoCodeAlreadyUsedException(@AuthenticationPrincipal JwtUser user) {
        log.info("Promo code applying failed. Cause: 'registration promo code already used'. User: '{}'", user);
        return errorResponse(REGISTRATION_PROMO_CODE_USED, properties.getPromoCodeUsedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OwnPromoCodeApplyingException.class)
    public JsonResponse handleOwnPromoCodeException(@AuthenticationPrincipal JwtUser user) {
        log.info("Promo code applying failed. Cause: 'trying to apply own promo code'. User: '{}'", user);
        return errorResponse(OWN_REG_PROMO_CODE, properties.getOwnPromoCodeMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PromoCodeAlreadyExistsException.class)
    public JsonResponse handlePromoCodeAlreadyExistsException(@AuthenticationPrincipal JwtUser user) {
        log.info("Changing promo code failed. Cause: 'code already exists'. User: '{}'", user);
        return errorResponse(REGISTRATION_PROMO_CODE_EXISTS, properties.getPromoCodeExistsMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PromoCodeAlreadyUsedException.class)
    public JsonResponse handleImpPromoCodeAlreadyUsedException(@AuthenticationPrincipal JwtUser user) {
        log.info("Applying promo code failed. Cause: 'code already used by user'. User: '{}'", user);
        return errorResponse(PROMO_CODE_USED, properties.getPromoCodeUsedMsg());
    }

/*
      @ResponseStatus(value = HttpStatus.OK)
      @ExceptionHandler(PaymentTransactionDeclinedException.class)
      public JsonResponse handlePaymentTransactionDeclinedException(PaymentTransactionDeclinedException ex) {
         log.info("Cloud payment transaction for user '{}' declined", ex.getUserNumber());
         return response(ex.getCardHolderMessage(), false, PAYMENT_TRANSACTION_DECLINED);
     }
*/
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CardBindingRequestSendingFailedException.class)
    public JsonResponse handleCardBindingRequestSendingFailedException(@AuthenticationPrincipal JwtUser user) {
        log.error("Card binding request sending failed. User: '{}'", user);
        return errorResponse(REQUEST_SENDING_FAILED, properties.getPaymentRequestSendingFailedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CardBindingRequestInvalidException.class)
    public JsonResponse handleCardBindingRequestInvalidException(CardBindingRequestInvalidException ex, @AuthenticationPrincipal JwtUser user) {
        log.info("Card binding request invalid. User: '{}'. Cause: '{}'", user, ex.getMessage());
        return errorResponse(REQUEST_SENDING_FAILED, properties.getPaymentRequestSendingFailedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CloudPaymentCardBindingTransactionDeclinedException.class)
    public JsonResponse handleCloudPaymentBindCardTransactionDeclinedException(CloudPaymentCardBindingTransactionDeclinedException ex,
                                                                               @AuthenticationPrincipal JwtUser user) {
        log.info("CloudPayment card bind transaction declined. User: '{}'. Cause: '{}'", user, ex.getDeclineReason());
        return errorResponse(CARD_BINDING_FAILED, ex.getDeclineReason());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CloudPaymentPayTransactionDeclinedException.class)
    public JsonResponse handleCloudPaymentTransactionDeclinedException(CloudPaymentPayTransactionDeclinedException ex,
                                                                       @AuthenticationPrincipal JwtUser user) {
        log.info("CloudPayment payment transaction declined. User: '{}'. Order: '{}'. Cause: '{}'", user,
                ex.getOrderId(), ex.getDeclineReason());
        return errorResponse(PAYMENT_TRANSACTION_DECLINED, ex.getDeclineReason());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(PaymentRequestSendingFailedException.class)
    public JsonResponse handlePaymentRequestSendingFailedException(@AuthenticationPrincipal JwtUser user) {
        log.info("Request sending failed. User: '{}'", user);
        return errorResponse(REQUEST_SENDING_FAILED, properties.getPaymentRequestSendingFailedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(PaymentRequestInvalidException.class)
    public JsonResponse handlePaymentRequestInvalidException(PaymentRequestInvalidException ex,
                                                             @AuthenticationPrincipal JwtUser user) {
        log.info("Payment request invalid. User: '{}'. Cause: '{}'", user, ex.getMessage());
        return errorResponse(REQUEST_SENDING_FAILED, properties.getPaymentRequestSendingFailedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(KitchenClosedException.class)
    public JsonResponse handleKitchenClosedException(@AuthenticationPrincipal JwtUser user) {
        log.info("Request failed - kitchen closed. User: '{}'", user);
        return errorResponse(KITCHEN_CLOSED, properties.getKitchenClosedMsg());
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(DeliveryZoneNotActiveException.class)
    public JsonResponse handleDeliveryZoneNotActiveException(@AuthenticationPrincipal JwtUser user) {
        log.info("Request failed - delivery zone not active. User: '{}'", user);
        return errorResponse(DELIVERY_ZONE_NOT_ACTIVE, properties.getDeliveryZoneNotActiveMsg());
    }

    private String logUser(JwtUser user) {
        return nonNull(user) ? user.toString() : "N/A";
    }
}
