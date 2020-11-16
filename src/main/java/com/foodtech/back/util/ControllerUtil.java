package com.foodtech.back.util;

import com.foodtech.back.dto.model.JsonResponse;

@SuppressWarnings("rawtypes")
public class ControllerUtil {

    private ControllerUtil() {
    }

    public static JsonResponse okResponse() {
        JsonResponse response = new JsonResponse();
        response.setSuccess(true);
        response.setCode(ResponseCode.OK.getCode());
        response.setMessage(ResponseCode.OK.toString());
        return response;
    }

    public static <T> JsonResponse<T> okResponse(T body) {
        JsonResponse<T> response = new JsonResponse<>();
        response.setSuccess(true);
        response.setCode(ResponseCode.OK.getCode());
        response.setMessage(ResponseCode.OK.toString());
        response.setBody(body);
        return response;
    }

    public static JsonResponse errorResponse(ResponseCode code, String userMsg) {
        JsonResponse response = new JsonResponse<>();
        response.setSuccess(false);
        response.setCode(code.getCode());
        response.setMessage(code.toString());
        response.setUserMessage(userMsg);
        return response;
    }
}
