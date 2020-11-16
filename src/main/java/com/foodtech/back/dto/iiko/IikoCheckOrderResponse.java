package com.foodtech.back.dto.iiko;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IikoCheckOrderResponse {

    public static final Integer IIKO_OK_CODE = 0;

    private Integer resultState; // 0 - OK
    private String problem;

    public IikoCheckOrderResponse(Integer state, String problem) {
        this.resultState = state;
        this.problem = problem;
    }

    public static IikoCheckOrderResponse sendingFailed() {
        return new IikoCheckOrderResponse(0, "Request sending failed");
    }

    public boolean orderCanBeSend() {
        return IIKO_OK_CODE.equals(resultState);
    }

}
