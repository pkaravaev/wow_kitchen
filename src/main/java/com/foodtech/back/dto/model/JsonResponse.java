package com.foodtech.back.dto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class JsonResponse<T> {

    private T body;
    private boolean success;
    private String code;
    private String message;
    private String userMessage;

}
