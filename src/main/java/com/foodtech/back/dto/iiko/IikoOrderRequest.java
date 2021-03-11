package com.foodtech.back.dto.iiko;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodtech.back.entity.model.*;
import com.foodtech.back.util.mapper.OrderMapper;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IikoOrderRequest {

    public static final IikoOrderItem CUTLERY = new IikoOrderItem("9fb95ef7-8136-4d59-8822-bbfd702bdef0", "Приборы", "12080", 1, 0);
    public static final String NO_NAME_USER = "Anonymous";
    public static final int IIKO_HOME_MAX_LENGTH = 9;
    public static final String TOO_LONG_HOME = "коммент.";

    private String organization;

    private String deliveryTerminalId;

    private IikoCustomer customer;

    private IikoOrder order;

    private IikoOrderRequest() {
        this.customer = new IikoCustomer();
        this.order = new IikoOrder();
        this.order.setPaymentItems(List.of(new IikoPaymentItem(new IikoPaymentType())));
        this.order.setAddress(new IikoAddressDto());
        this.order.setItems(new ArrayList<>());
    }

    public static IikoOrderRequest form(Order order) {
        IikoOrderRequest request = new IikoOrderRequest();
        formKitchenData(request, order.getAddress().getDeliveryTerminal());
        formCustomerData(request, order.getUser());
        formOrderData(request, order);
        return request;
    }

    private static void formKitchenData(IikoOrderRequest request, KitchenDeliveryTerminal terminal) {
        Kitchen kitchen = terminal.getKitchen();
        request.setOrganization(kitchen.getOrganizationId());
        request.setDeliveryTerminalId(terminal.getTerminalId());
        IikoPaymentType paymentType = request.getOrder().getPaymentItems().get(0).getPaymentType();
        paymentType.setId(kitchen.getPaymentTypeId());
        paymentType.setCode(kitchen.getPaymentTypeCode());
        paymentType.setName(kitchen.getPaymentTypeName());
        paymentType.setExternalRevision(kitchen.getPaymentTypeExternalRevision());
        paymentType.setCombinable(kitchen.isPaymentTypeCombinable());
        paymentType.setDeleted(kitchen.isPaymentTypeDeleted());
        paymentType.setComment("");
    }

    private static void formCustomerData(IikoOrderRequest request, User user) {
        request.getCustomer().setName(StringUtils.isEmpty(user.getName()) ? NO_NAME_USER : user.getName());
        request.getCustomer().setPhone(user.toString());
    }

    private static void formOrderData(IikoOrderRequest request, Order order) {
        request.getOrder().setPhone(order.getUser().toString());
        request.getOrder().getPaymentItems().get(0).setSum(order.getTotalCost());
        formAddress(request.getOrder().getAddress(), order.getAddress());
        request.getOrder().setItems(OrderMapper.toIikoOrderItems(order.getItems()));
        if (order.isCutlery()) {
            request.getOrder().getItems().add(CUTLERY);
        }
    }

    private static void formAddress(IikoAddressDto iikoAddress, Address userAddress) {
        iikoAddress.setCity(userAddress.getCity());
        iikoAddress.setStreet(userAddress.getStreet());
        iikoAddress.setApartment(userAddress.getApartment());
        iikoAddress.setFloor(userAddress.getFloor());
        iikoAddress.setEntrance(userAddress.getEntrance());
        if (userAddress.getHome().length() > IIKO_HOME_MAX_LENGTH) {
            iikoAddress.setHome(TOO_LONG_HOME); // указываем оператору iiko, что номер дома нужно смотреть в комментарии к заказу
            iikoAddress.setComment(userAddress.getHome());
        } else {
            iikoAddress.setHome(userAddress.getHome());
        }
    }
}
