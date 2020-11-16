package com.foodtech.back.service.http;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.payment.cloud.CloudPaymentBaseResponse;
import com.foodtech.back.dto.payment.cloud.CloudPaymentRequest;
import com.foodtech.back.dto.payment.cloud.CloudPaymentRequestType;
import com.foodtech.back.util.exceptions.CardBindingRequestSendingFailedException;
import com.foodtech.back.util.exceptions.PaymentRequestSendingFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CloudPaymentRequestSender extends HttpSender {

    private final ResourcesProperties properties;

    public CloudPaymentRequestSender(RestTemplateBuilder restTemplateBuilder, ResourcesProperties properties) {
        super(restTemplateBuilder);
        this.properties = properties;
    }

    public CloudPaymentBaseResponse sendCardBindRequest(CloudPaymentRequest request) {
        try {
            return sendPostRequest(request);
        } catch (Exception ex) {
            throw new CardBindingRequestSendingFailedException();
        }
    }

    public CloudPaymentBaseResponse sendPaymentRequest(CloudPaymentRequest request) {
        try {
            return sendPostRequest(request);
        } catch (Exception ex) {
            throw new PaymentRequestSendingFailedException();
        }
    }

    public CloudPaymentBaseResponse sendPaResRequest(CloudPaymentRequest request) {
        try {
            return sendPostRequest(request);
        } catch (Exception ex) {
            log.error("CloudPayment request sending failed. Request: '{}'. Cause: '{}'", request.getType(), ex.getMessage());
            return null;
        }
    }

    public CloudPaymentBaseResponse sendAuthFinalizeRequest(CloudPaymentRequest request) {
        try {
            return sendPostRequest(request);
        } catch (Exception ex) {
            log.error("CloudPayment request sending failed. Request: '{}'. Cause: '{}'", request.getType(), ex.getMessage());
            return null;
        }
    }

    private CloudPaymentBaseResponse sendPostRequest(CloudPaymentRequest request) {
        return post(formRequestUrl(request.getType()), request, CloudPaymentBaseResponse.class, MediaType.APPLICATION_JSON, basicAuth());
    }

    private String formRequestUrl(CloudPaymentRequestType type) {
        String url = properties.getCloudBaseUrl();
        switch (type) {
            case CARD_BIND:
            case GOOGLE_PAY_AUTH:
                return url + properties.getCloudAuthCryptPath();
            case AUTH:
                return url + properties.getCloudAuthTokenPath();
            case CONFIRM_AUTH:
                return url + properties.getCloudConfirmPath();
            case CANCEL_AUTH:
                return url + properties.getCloudCancelPath();
            case PA_RES:
                return url+ properties.getCloudPaResPath();
            default:
                throw new IllegalArgumentException();
        }
    }

    private BasicAuth basicAuth() {
        return new BasicAuth(properties.getCloudPublicId(), properties.getCloudApiKey());
    }

    public CloudPaymentBaseResponse sendTestRequest() {
        try {
            return get(properties.getCloudTestUrl(), CloudPaymentBaseResponse.class, basicAuth());
        } catch (Exception ex) {
            log.error("CloudPayment test request sending failed. Cause: '{}'", ex.getMessage());
            return null;
        }
    }
}
