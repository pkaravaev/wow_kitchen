package com.foodtech.back.dto.iiko;

import lombok.Getter;

import java.util.Objects;

@Getter
public class IikoOrderSendingResult {

    private boolean success;
    private String problem;
    private IikoOrderInfo orderInfo;

    private IikoOrderSendingResult(boolean success, String problem, IikoOrderInfo orderInfo) {
        this.success = success;
        this.problem = problem;
        this.orderInfo = orderInfo;
    }

    public static IikoOrderSendingResult success(IikoOrderInfo orderInfo) {
        // Если заказ зарегистрирован в системе iiko успешно, но есть проблемы
        if (Objects.nonNull(orderInfo.getProblem()) && orderInfo.getProblem().getHasProblem()) {
            return new IikoOrderSendingResult(true, orderInfo.getProblem().getProblem(), orderInfo);
        }

        // Если заказ зарегистрирован успешно, без проблем
        return new IikoOrderSendingResult(true, null, orderInfo);
    }

    public static IikoOrderSendingResult failed(String problem) {
        return new IikoOrderSendingResult(false, problem, null);
    }

    public static IikoOrderSendingResult failed() {
        return new IikoOrderSendingResult(false, null, null);
    }
}
