package com.foodtech.back.entity.bonus;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Embeddable;
import javax.validation.constraints.Min;

@Embeddable
@Data
public class BonusAccount {

    @Min(0)
    private int bonusAmount;

    private int bonusesSpent; // число потраченных бонусов

    @Length(min = 5)
    private String registrationPromoCode; // промо-код, которым можно поделиться при приглашении нового юзера

    private boolean registrationPromoCodeUsed; // введен ли рег. промо-код, полученный от другого юзера

    private Long registrationPromoCodeOwnerUserId; // юзер, пригласивший данного юзера через рег. промо-код
    // (из соображений производительности не создаем OneToOne отношение)

    private boolean registrationPromoCashbackReturned; // выплачен ли приглашенному юзеру бонус за приглашение

    public void addBonuses(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bonuses amount can't be negative number.");
        }
        setBonusAmount(getBonusAmount() + amount);
    }

    public void writeOffBonuses(int bonusAmount) {
        int newValue = getBonusAmount() - bonusAmount;
        if (bonusAmount < 0 || newValue < 0) {
            throw new IllegalArgumentException("Bonuses amount can't be negative number.");
        }
        setBonusAmount(newValue);
    }

    public void addBonusesSpent(int bonusesSpent) {
        setBonusesSpent(getBonusesSpent() + bonusesSpent);
    }

}
