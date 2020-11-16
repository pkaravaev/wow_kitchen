package com.foodtech.back.service.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.nonNull;

public abstract class HttpSender {

    private final RestTemplateBuilder restTemplateBuilder;

    public HttpSender(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public <T, R> R post(String url, T requestDto, Class<R> responseClass, MediaType mediaType, BasicAuth basicAuth) {
        RestTemplate template = restTemplateBuilder.build();
        HttpHeaders headers = new HttpHeaders();
        if (nonNull(basicAuth)) {
            headers.setBasicAuth(basicAuth.getUsername(), basicAuth.getPassword());
        }
        headers.setContentType(mediaType);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<R> response = template.exchange(url, HttpMethod.POST, requestEntity, responseClass);

        return response.getBody();

    }

    public <R> R get(String uri, Class<R> responseClass, BasicAuth basicAuth) {
        RestTemplate template = restTemplateBuilder.build();
        HttpHeaders headers = new HttpHeaders();
        if (nonNull(basicAuth)) {
            headers.setBasicAuth(basicAuth.getUsername(), basicAuth.getPassword());
        }
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<R> response = template.exchange(uri, HttpMethod.GET, requestEntity, responseClass);

        return response.getBody();

    }


}
