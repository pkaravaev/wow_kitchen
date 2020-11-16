package com.foodtech.back.service.bonus;

import com.foodtech.back.service.event.OrderCancelledEvent;
import com.foodtech.back.service.event.OrderClosedEvent;
import com.foodtech.back.service.event.OrderNotConfirmedEvent;
import com.foodtech.back.service.event.OrderSendingFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class BonusServiceEventListeners {

    private final BonusService bonusService;

    public BonusServiceEventListeners(BonusService bonusService) {
        this.bonusService = bonusService;
    }

    @EventListener
    public void checkAndGiveBonusesToPromoCodeOwner(OrderClosedEvent event) {
        bonusService.checkAndGiveBonusesToPromoCodeOwner(event.getOrder().getUser().getId());
    }

    @EventListener
    public void orderSendingFailedListener(OrderSendingFailedEvent event) {
        bonusService.returnBonuses(event.getOrder().getId());
    }

    @EventListener
    public void orderCancelledListener(OrderCancelledEvent event) {
        bonusService.returnBonuses(event.getOrder().getId());
    }

    @EventListener
    public void orderSendingFailedListener(OrderNotConfirmedEvent event) {
        bonusService.returnBonuses(event.getOrder().getId());
    }
}
