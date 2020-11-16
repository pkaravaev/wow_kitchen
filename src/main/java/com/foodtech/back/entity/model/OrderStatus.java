package com.foodtech.back.entity.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum OrderStatus {
    // iikoStatus - наименование статуса, приходящее из iiko
    NEW("", ""),
    PAYMENT_FAILED("", ""),
    PAID("", ""),
    AUTHORIZED("", ""),
    NOT_PAID("", ""),
    PAYMENT_DECLINED("", ""),
    BONUS_COLLISION("", ""),
    ON_SENDING("", ""),
    SENDING_FAILED("", ""),
    SENT("Новая", "New"),
    IN_PROGRESS("Готовится", "In progress"),
    READY("Готово", "Ready"),
    AWAITING_DELIVERY("Ждет отправки", "Awaiting delivery"),
    ON_THE_WAY("В пути", "On the way"),
    DELIVERED("Доставлена", "Delivered"),
    CLOSED("Закрыта", "Closed"),
    NOT_CONFIRMED("Не подтверждена", "Not confirmed"),
    CANCELLED("Отменена", "Cancelled"),
    NOT_PROCESSED("", "");

    private String iikoStatusRus;

    private String iikoStatusEng;

    OrderStatus(String iikoStatusRus, String iikoStatusEng) {
        this.iikoStatusRus = iikoStatusRus;
        this.iikoStatusEng = iikoStatusEng;
    }

    public String getIikoStatusRus() {
        return iikoStatusRus;
    }

    public String getIikoStatusEng() {
        return iikoStatusEng;
    }

    public static OrderStatus parseIikoStatus(String iikoStatus) {
        return Arrays.stream(values())
                .filter(s -> s.getIikoStatusRus().equalsIgnoreCase(iikoStatus)
                        || s.getIikoStatusEng().equalsIgnoreCase(iikoStatus))
                .findFirst().orElseThrow();
    }

    public static List<OrderStatus> forOrderHistory() {
        return List.of(PAID, SENT, IN_PROGRESS, READY, AWAITING_DELIVERY, ON_THE_WAY, DELIVERED, CLOSED, NOT_CONFIRMED, CANCELLED);
    }

    public static Set<OrderStatus> forPaymentCompletion() {
        return Set.of(DELIVERED, CLOSED);
    }

    public static Set<OrderStatus> forPaymentCancelling() {
        return Set.of(CANCELLED, BONUS_COLLISION, SENDING_FAILED);
    }
}
