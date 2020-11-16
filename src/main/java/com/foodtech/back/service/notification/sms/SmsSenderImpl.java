package com.foodtech.back.service.notification.sms;

import com.foodtech.back.config.ResourcesProperties;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Data
@Service
public class SmsSenderImpl implements SmsSender {

    //TODO добавить логирование

    private static final String SENDING_SMS = "sms.sending";
    private static final String SMS_SENT = "sms.sent";
    private static final String ERROR_CREATING_URL = "sms.error.url";

    private static final String SMS_LOGIN = "login";
    private static final String SMS_PASSWORD = "psw";
    private static final String SMS_PHONES = "phones";
    private static final String SMS_MESSAGES = "mes";
    private static final String SMS_SENDER = "sender";
    private static final String SMS_CHARSET = "charset";
    private static final String REST_TEMPLATE_BODY = "";

    private final ResourcesProperties properties;

    public SmsSenderImpl(ResourcesProperties properties) {
        this.properties = properties;
    }

    @Override
    public SmsMessageDto send(SmsMessageDto dto) {
        HttpStatus response = new RestTemplate().exchange(
                createUrlWithParams(dto), HttpMethod.GET, new HttpEntity<>(REST_TEMPLATE_BODY, new HttpHeaders()), String.class)
                .getStatusCode();
        dto.setResponse(response.toString());
        dto.setSuccess(response.equals(HttpStatus.OK));
        return dto;
    }

    private URI createUrlWithParams(SmsMessageDto dto) {
        URI url = null;
        try {
            url = new URIBuilder()
                    .setScheme(properties.getSmsUrlScheme())
                    .setPath(properties.getSmsUrlPath())
                    .addParameters(getParameters(dto))
                    .setHost(properties.getSmsUrlHost()).build();
        } catch (URISyntaxException e) {
//            LOGGER.error(messageSource.getMessage(ERROR_CREATING_URL, new String[]{}, locale), dto.getCountry(), dto.getMobNumber());
        }
        return url;
    }

    private List<NameValuePair> getParameters(SmsMessageDto dto) {
        return List.of(
                new BasicNameValuePair(SMS_LOGIN, properties.getSmsLogin()),
                new BasicNameValuePair(SMS_PASSWORD, properties.getSmsPassword()),
                new BasicNameValuePair(SMS_PHONES, dto.getCountry() + dto.getMobNumber()),
                new BasicNameValuePair(SMS_MESSAGES, dto.getContent()),
                new BasicNameValuePair(SMS_CHARSET, properties.getSmsCharset())
        );
    }


}
