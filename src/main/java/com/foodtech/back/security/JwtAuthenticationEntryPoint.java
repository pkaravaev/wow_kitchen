package com.foodtech.back.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtech.back.config.ResourcesProperties;
import com.foodtech.back.dto.model.JsonResponse;
import com.foodtech.back.util.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.foodtech.back.security.JwtAuthorizationTokenFilter.TOKEN_ERROR;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;

    private final ObjectMapper objectMapper;

    private final ResourcesProperties properties;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper, ResourcesProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /* Кастомно обрабатываем ошибки авторизации */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        JsonResponse<Object> jsonResponse = new JsonResponse<>();

        ResponseCode responseCode = ResponseCode.UNAUTHORIZED;

        /* Этот кастомный атрибут проставляется в JwtAuthorizationTokenFilter
            в виде стрингового отражения кастомных ResponseCode */
        String tokenError = (String) request.getAttribute(TOKEN_ERROR);

        /* Формируем сообщение для пользователя */
        String userMsg = "";
        if (Objects.nonNull(tokenError)) {
            responseCode = ResponseCode.valueOf(tokenError);
            userMsg = (responseCode == ResponseCode.TOKEN_EXPIRED) ? null : properties.getDefaultErrorMsg();
        }

        jsonResponse.setCode(responseCode.getCode());
        jsonResponse.setMessage(responseCode.toString());
        jsonResponse.setUserMessage(userMsg);

        String responseJson = objectMapper.writeValueAsString(jsonResponse);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().write(responseJson);
    }
}