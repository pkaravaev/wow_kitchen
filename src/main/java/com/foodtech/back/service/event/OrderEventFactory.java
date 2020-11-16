package com.foodtech.back.service.event;

public class OrderEventFactory {

//    private static final Map<OrderStatus, TriFunction<Long, Long, String, OrderStatusChangedEvent>> EVENTS = new HashedMap<>();
//
//    static {
//        EVENTS.put(PAID, OrderPaidEvent::new);
//        EVENTS.put(NOT_PAID, OrderPaidEvent::new);
//        EVENTS.put(BONUS_COLLISION, BonusCollisionEvent::new);
//        EVENTS.put(SENT, OrderSentEvent::new);
//        EVENTS.put(SENDING_FAILED, OrderSendingFailedEvent::new);
//        EVENTS.put(IN_PROGRESS, OrderInProgressEvent::new);
//        EVENTS.put(ON_THE_WAY, OrderOnTheWayEvent::new);
//        EVENTS.put(DELIVERED, OrderDeliveredEvent::new);
//        EVENTS.put(CLOSED, OrderClosedEvent::new);
//        EVENTS.put(CANCELLED, OrderCancelledEvent::new);
//        EVENTS.put(NOT_CONFIRMED, OrderNotConfirmedEvent::new);
//        EVENTS.put(NOT_PROCESSED, OrderNotProcessedEvent::new);
//    }
//
//    private OrderEventFactory() {
//    }
//
//    public static OrderStatusChangedEvent about(OrderStatus status, Long orderId, Long userId, String info) {
//        TriFunction<Long, Long, String, OrderStatusChangedEvent> createFunction = EVENTS.get(status);
//
//        if (isNull(createFunction)) {
//            throw new IllegalArgumentException("No order status event found for: " + status);
//        }
//
//        return createFunction.apply(orderId, userId, info);
//    }
//
//    public static OrderStatusChangedEvent about(OrderStatus status, Long orderId, Long userId) {
//        return about(status, orderId, userId, null);
//    }
}
