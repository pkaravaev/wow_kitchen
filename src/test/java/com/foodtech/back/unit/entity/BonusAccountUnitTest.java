package com.foodtech.back.unit.entity;

import com.foodtech.back.entity.bonus.BonusAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BonusAccountUnitTest {

    @Test
    void addBonuses_1() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(100);

        account.addBonuses(200);

        assertEquals(300, account.getBonusAmount());
    }

    @Test
    void addBonuses_2() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(0);

        account.addBonuses(33);

        assertEquals(33, account.getBonusAmount());
    }

    @Test
    void addBonuses_3() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(56);

        account.addBonuses(0);

        assertEquals(56, account.getBonusAmount());
    }

    @Test
    void addBonusesNegative() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(100);

        assertThrows(IllegalArgumentException.class, () -> account.addBonuses(-200));
    }

    @Test
    void writeOffBonuses_1() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(178);

        account.writeOffBonuses(34);

        assertEquals(144, account.getBonusAmount());
    }

    @Test
    void writeOffBonuses_2() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(178);

        account.writeOffBonuses(0);

        assertEquals(178, account.getBonusAmount());
    }

    @Test
    void writeOffBonusesNegative_1() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(376);

        assertThrows(IllegalArgumentException.class, () -> account.writeOffBonuses(-200));
    }

    @Test
    void writeOffBonusesNegative_2() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(462);

        assertThrows(IllegalArgumentException.class, () -> account.writeOffBonuses(567));
    }

}