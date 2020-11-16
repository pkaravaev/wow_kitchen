package com.foodtech.back.service.http;

import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.iiko.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Slf4j
public class IikoHttpSender extends HttpSender {

    private final ResourcesProperties properties;

    public IikoHttpSender(RestTemplateBuilder restTemplateBuilder, ResourcesProperties properties) {
        super(restTemplateBuilder);
        this.properties = properties;
    }

    private String getIikoAccessToken() {
        String url = properties.getIikoBaseUrl() + "auth/access_token";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("user_id", properties.getIikoUserId())
                .queryParam("user_secret", properties.getIikoUserSecret());

        return get(uriBuilder.toUriString(), String.class, null).replaceAll("\"", "");
    }

    public IikoNomenclatureDto sendNomenclatureRequest() {
        String url = properties.getIikoBaseUrl() + "nomenclature/" + properties.getIikoOrganizationId();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("access_token", getIikoAccessToken());
        return get(uriBuilder.toUriString(), IikoNomenclatureDto.class, null);
    }

    public IikoStopListDto sendStopListRequest() {
        String url = properties.getIikoBaseUrl() + "stopLists/getDeliveryStopList";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("access_token", getIikoAccessToken())
                .queryParam("organization", properties.getIikoOrganizationId());

        try {
            return get(uriBuilder.toUriString(), IikoStopListDto.class, null);
        } catch (RestClientException ex) {
            log.error("Http send failed for get stop list request, cause: '{}'", ex.getMessage());
            return null;
        }
    }

    public List<IikoDeliveryZoneDto> sendDeliveryZoneRequest() {
        String url = properties.getIikoBaseUrl() + "deliverySettings/getDeliveryRestrictions";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("access_token", getIikoAccessToken())
                .queryParam("organization", properties.getIikoOrganizationId());
        IikoDeliveryRestrictionsDto deliveryRestrictionsDto = get(uriBuilder.toUriString(), IikoDeliveryRestrictionsDto.class, null);
        return deliveryRestrictionsDto.getDeliveryZones();
    }

    public IikoCheckOrderResponse sendCheckOrderRequest(IikoOrderRequest requestEntity) {
        String url = properties.getIikoBaseUrl() + "orders/checkCreate";
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("access_token", getIikoAccessToken());
            return post(uriBuilder.toUriString(), requestEntity, IikoCheckOrderResponse.class, MediaType.APPLICATION_JSON, null);
        } catch (RestClientException ex) {
            log.error("Http send failed for check order request, cause: '{}'", ex.getMessage());
            return IikoCheckOrderResponse.sendingFailed();
        }
    }

    public IikoOrderInfo sendOrderRequest(IikoOrderRequest requestEntity) {
        String url = properties.getIikoBaseUrl() + "orders/add";
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("access_token", getIikoAccessToken());
            return post(uriBuilder.toUriString(), requestEntity, IikoOrderInfo.class, MediaType.APPLICATION_JSON, null);
        } catch (RestClientException ex) {
            log.error("Http send failed for order request, cause: '{}'", ex.getMessage());
            return null;
        }
    }

    public IikoOrderInfo sendGetOrderInfo(String iikoOrderId) {
        String url = properties.getIikoBaseUrl() + "orders/info";
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("access_token", getIikoAccessToken())
                    .queryParam("organization", properties.getIikoOrganizationId())
                    .queryParam("order", iikoOrderId);

            return get(uriBuilder.toUriString(), IikoOrderInfo.class, null);
        } catch (RestClientException ex) {
            log.error("Http send failed for order info request, cause: '{}'", ex.getMessage());
            return null;
        }
    }

    // Код этого метода получения справочника улиц приводит к крашу компиляции на 70-м тестовом серваке (как-то связано с дженериками),
    // баг задокументирован - https://bugs.openjdk.java.net/browse/JDK-8222754 и вроде как пофикшен в 11 джаве,
    // но на 70-м почему-то все равно вылезает, хотя отлично работает локально.
    // Так как справочник не подразумевает постоянного обновления, оставим пока как есть в закомменченном виде.

//    получение справочника городов/улиц
    public List<IikoCityStreetsDirectoryDto> sendCityStreetsDirectoryRequest() {
//        String url = properties.getIikoBaseUrl() + "cities/cities";
//        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("access_token", getIikoAccessToken())
//                .queryParam("organization", properties.getIikoOrganizationId());
//
//        RestTemplate template = restTemplateBuilder.build();
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity requestEntity = new HttpEntity(headers);
//        ResponseEntity<List<IikoCityStreetsDirectoryDto>> response = template.exchange(uriBuilder.toUriString(),
//                HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {});
//        return response.getBody();
        return null;
    }
}
