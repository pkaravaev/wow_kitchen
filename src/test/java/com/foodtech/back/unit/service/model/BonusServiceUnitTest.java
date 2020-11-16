package com.foodtech.back.unit.service.model;

import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.bonus.PromoCodeImpersonal;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.repository.bonus.PromoCodeImpersonalRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.repository.model.UserRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.unit.AbstractUnitTest;
import com.foodtech.back.util.exceptions.*;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Optional;

import static com.foodtech.back.service.bonus.BonusService.REGISTRATION_PROMO_CODE_BONUS_AMOUNT;
import static com.foodtech.back.service.bonus.BonusService.REG_PROMO_CODE_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BonusServiceUnitTest extends AbstractUnitTest {

    @Autowired
    BonusService bonusService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    PromoCodeImpersonalRepository impersonalRepository;

    @Test
    void createBonusAccount() {
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(anyString())).thenReturn(false);

        BonusAccount createdAccount = bonusService.createBonusAccount();

        assertCreatedBonusAccountIsCorrect(createdAccount);
        verify(userRepository, times(1)).existsByBonusAccountRegistrationPromoCodeEquals(anyString());
    }

    @Test
    void createBonusAccountCodeExisted() {
        int codeGenMaxAttempt = 3;
        // имитируем ситуацию, когда генератор промо-кода сгенерировал уже существующий код
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(anyString())).thenAnswer(new Answer<>() {
            private int codeGenAttempt = 0;

            @Override
            public Object answer(InvocationOnMock invocation) {
                return codeGenAttempt++ < codeGenMaxAttempt;
            }
        });

        BonusAccount createdAccount = bonusService.createBonusAccount();

        assertCreatedBonusAccountIsCorrect(createdAccount);
        verify(userRepository, times(codeGenMaxAttempt + 1)).existsByBonusAccountRegistrationPromoCodeEquals(anyString());
    }

    @Test
    void applyRegPromoCode() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        String code = "BBBBB";
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.of(CODE_OWNER_ID));

        int bonusAmount = bonusService.applyPromoCode(code, user.getId());

        assertEquals(bonusAccount().getBonusAmount() + REGISTRATION_PROMO_CODE_BONUS_AMOUNT, bonusAmount);
        assertThat(account).isEqualToComparingFieldByField(bonusAccountAfterApplyingRegPromoCode());
    }

    @Test
    void applyOwnCode() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        String code = account.getRegistrationPromoCode();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.of(user.getId()));

        assertThrows(OwnPromoCodeApplyingException.class, () -> bonusService.applyPromoCode(code, user.getId()));
        assertThat(account).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void applyUsedCode() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        account.setRegistrationPromoCodeUsed(true);
        String code = "BBBBB";
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.of(CODE_OWNER_ID));

        assertThrows(RegistrationPromoCodeAlreadyUsedException.class, () -> bonusService.applyPromoCode(code, user.getId()));
        assertThat(account).isEqualToIgnoringGivenFields(bonusAccount(), "registrationPromoCodeUsed");
    }

    @Test
    void applyImpersonal() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        PromoCodeImpersonal impersonal = codeImpersonal();
        String code = impersonal.getPromoCode();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.empty());
        when(impersonalRepository.findByPromoCodeEquals(code)).thenReturn(Optional.of(impersonal));

        int bonusAmount = bonusService.applyPromoCode(code, user.getId());

        assertEquals(bonusAccount().getBonusAmount() + impersonal.getAmount(), bonusAmount);
        assertEquals(bonusAccount().getBonusAmount() + impersonal.getAmount(), account.getBonusAmount());
        assertEquals(1, user.getUsedPromoCodes().size());
        assertThat(user.getUsedPromoCodes().get(0)).isEqualToIgnoringGivenFields(impersonal, "usedByUsers");
        assertThat(account).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount");
    }

    @Test
    void applyImpersonalNotFound() {
        User user = user();
        String code = "NTF";
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.empty());
        when(impersonalRepository.findByPromoCodeEquals(code)).thenReturn(Optional.empty());

        assertThrows(PromoCodeNotFoundException.class, () -> bonusService.applyPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount());
    }

    @Test
    void applyImpersonalAlreadyUsed() {
        User user = user();
        PromoCodeImpersonal impersonal = codeImpersonal();
        user.getUsedPromoCodes().add(impersonal);
        String code = impersonal.getPromoCode();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findPromoCodeOwnerId(code)).thenReturn(Optional.empty());
        when(impersonalRepository.findByPromoCodeEquals(code)).thenReturn(Optional.of(impersonal));

        assertThrows(PromoCodeAlreadyUsedException.class, () -> bonusService.applyPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void changeRegCode() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "NEW_CODE";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        bonusService.changeRegistrationPromoCode(code, user.getId());

        assertEquals(code, user.getBonusAccount().getRegistrationPromoCode());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "registrationPromoCode");
    }

    @Test
    void changeRegCodeWithSpaces() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "NEW     _COD    E";
        String codeNoSpaces = "NEW_CODE";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        bonusService.changeRegistrationPromoCode(code, user.getId());

        assertEquals(codeNoSpaces, user.getBonusAccount().getRegistrationPromoCode());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "registrationPromoCode");
    }

    @Test
    void changeRegCodeInvalidMinLength() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "NEW";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        assertThrows(ConstraintViolationException.class, () -> bonusService.changeRegistrationPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void changeRegCodeInvalidMaxLength() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "NEWWWWWWWWWWWWWWWWWWWWWW";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        assertThrows(ConstraintViolationException.class, () -> bonusService.changeRegistrationPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void changeRegCodeInvalidWhitespaces() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "N      ";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        assertThrows(ConstraintViolationException.class, () -> bonusService.changeRegistrationPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void changeRegCodeInvalidOnlyWhitespaces() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "       ";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(false);

        assertThrows(ConstraintViolationException.class, () -> bonusService.changeRegistrationPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void changeRegCodeAlreadyExists() {
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String code = "NEW_CODE";
        when(userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)).thenReturn(true);

        assertThrows(PromoCodeAlreadyExistsException.class, () -> bonusService.changeRegistrationPromoCode(code, user.getId()));
        assertThat(user.getBonusAccount()).isEqualToComparingFieldByField(bonusAccount());
    }

    @Test
    void writeOff() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 500;
        account.setBonusAmount(amountBefore);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        int appliedBonusAmount = 100;
        boolean success = bonusService.writeOffBonuses(user.getId(), appliedBonusAmount);

        assertTrue(success);
        assertEquals(amountBefore - appliedBonusAmount, account.getBonusAmount());
        assertEquals(appliedBonusAmount, account.getBonusesSpent());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount", "bonusesSpent");
    }

    @Test
    void writeOffBonusAmountLessThanApplied() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 500;
        account.setBonusAmount(amountBefore);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        int appliedBonusAmount = 1000;
        boolean success = bonusService.writeOffBonuses(user.getId(), appliedBonusAmount);

        assertFalse(success);
        assertEquals(amountBefore, account.getBonusAmount());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount");
    }

    @Test
    void writeOffZero() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 500;
        account.setBonusAmount(amountBefore);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        int appliedBonusAmount = 0;
        boolean success = bonusService.writeOffBonuses(user.getId(), appliedBonusAmount);

        assertTrue(success);
        assertEquals(amountBefore - appliedBonusAmount, account.getBonusAmount());
        assertEquals(appliedBonusAmount, account.getBonusesSpent());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount");
    }

    @Test
    void writeOffNegative() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 500;
        account.setBonusAmount(amountBefore);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        int appliedBonusAmount = -100;
        boolean success = bonusService.writeOffBonuses(user.getId(), appliedBonusAmount);

        assertFalse(success);
        assertEquals(amountBefore, account.getBonusAmount());
        assertThat(user.getBonusAccount()).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount");
    }

    @Test
    void giveBonusesToOwner_case1() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        account.setBonusesSpent(REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
        account.setRegistrationPromoCodeUsed(true);
        account.setRegistrationPromoCodeOwnerUserId(CODE_OWNER_ID);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        User codeOwner = codeOwner();
        int ownerBonusAmountBefore = codeOwner.getBonusAccount().getBonusAmount();
        when(userRepository.findById(CODE_OWNER_ID)).thenReturn(Optional.of(codeOwner));

        bonusService.checkAndGiveBonusesToPromoCodeOwner(user.getId());

        assertTrue(account.isRegistrationPromoCashbackReturned());
        assertTrue(account.isRegistrationPromoCodeUsed());

        assertEquals(ownerBonusAmountBefore + REGISTRATION_PROMO_CODE_BONUS_AMOUNT, codeOwner.getBonusAccount().getBonusAmount());
        assertThat(codeOwner.getBonusAccount()).isEqualToIgnoringGivenFields(codeOwner().getBonusAccount(), "bonusAmount");
    }

    @Test
    void giveBonusesToOwner_case2() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        account.setBonusesSpent(REGISTRATION_PROMO_CODE_BONUS_AMOUNT + 1);
        account.setRegistrationPromoCodeUsed(true);
        account.setRegistrationPromoCodeOwnerUserId(CODE_OWNER_ID);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        User codeOwner = codeOwner();
        int ownerBonusAmountBefore = codeOwner.getBonusAccount().getBonusAmount();
        when(userRepository.findById(CODE_OWNER_ID)).thenReturn(Optional.of(codeOwner));

        bonusService.checkAndGiveBonusesToPromoCodeOwner(user.getId());

        assertTrue(account.isRegistrationPromoCashbackReturned());
        assertTrue(account.isRegistrationPromoCodeUsed());

        assertEquals(ownerBonusAmountBefore + REGISTRATION_PROMO_CODE_BONUS_AMOUNT, codeOwner.getBonusAccount().getBonusAmount());
        assertThat(codeOwner.getBonusAccount()).isEqualToIgnoringGivenFields(codeOwner().getBonusAccount(), "bonusAmount");
    }

    @Test
    void giveBonusesToOwner_SpentNotReachedRequiredValue() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        account.setBonusesSpent(REGISTRATION_PROMO_CODE_BONUS_AMOUNT - 1);
        account.setRegistrationPromoCodeUsed(true);
        account.setRegistrationPromoCodeOwnerUserId(CODE_OWNER_ID);
        User codeOwner = codeOwner();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(CODE_OWNER_ID)).thenReturn(Optional.of(codeOwner));

        bonusService.checkAndGiveBonusesToPromoCodeOwner(user.getId());

        assertFalse(account.isRegistrationPromoCashbackReturned());
        assertTrue(account.isRegistrationPromoCodeUsed());

        //проверяем, что владельцу промо-кода не начислены баллы
        assertThat(codeOwner).isEqualToComparingFieldByField(codeOwner());
    }

    @Test
    void giveBonusesToOwner_RegCodeUsedButBonusesAlreadyReturned() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        account.setBonusesSpent(REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
        account.setRegistrationPromoCodeUsed(true);
        account.setRegistrationPromoCashbackReturned(true);
        account.setRegistrationPromoCodeOwnerUserId(CODE_OWNER_ID);
        User codeOwner = codeOwner();
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(CODE_OWNER_ID)).thenReturn(Optional.of(codeOwner));

        bonusService.checkAndGiveBonusesToPromoCodeOwner(user.getId());

        assertTrue(account.isRegistrationPromoCashbackReturned());
        assertTrue(account.isRegistrationPromoCodeUsed());

        //проверяем, что владельцу промо-кода не начислены баллы
        assertThat(codeOwner).isEqualToComparingFieldByField(codeOwner());
    }

    @Test
    void returnBonuses() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 100;
        account.setBonusAmount(amountBefore);
        int spentBefore = 300;
        account.setBonusesSpent(spentBefore);

        Order order = new Order();
        Long orderId = 1L;
        order.setUser(user);
        int returnAmount = 200;
        order.setAppliedBonusAmount(returnAmount);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        bonusService.returnBonuses(orderId);

        assertEquals(amountBefore + returnAmount, account.getBonusAmount());
        assertEquals(spentBefore - returnAmount, account.getBonusesSpent());
        assertThat(account).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount", "bonusesSpent");
    }

    @Test
    void returnBonusesSpentNegative() {
        User user = user();
        BonusAccount account = user.getBonusAccount();
        int amountBefore = 100;
        account.setBonusAmount(amountBefore);
        int spentBefore = 0;
        account.setBonusesSpent(spentBefore);

        Order order = new Order();
        Long orderId = 1L;
        order.setUser(user);
        int returnAmount = 200;
        order.setAppliedBonusAmount(returnAmount);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        bonusService.returnBonuses(orderId);

        assertEquals(amountBefore + returnAmount, account.getBonusAmount());
        assertEquals(0, account.getBonusesSpent());
        assertThat(account).isEqualToIgnoringGivenFields(bonusAccount(), "bonusAmount", "bonusesSpent");
    }

    @Test
    void countBonusAmountForCart() {
        assertEquals(306, bonusService.countCartCostWithBonuses(611, 500));
        assertEquals(411, bonusService.countCartCostWithBonuses(611, 200));
        assertEquals(200, bonusService.countCartCostWithBonuses(400, 200));
        assertEquals(400, bonusService.countCartCostWithBonuses(400, 0));
        assertEquals(0, bonusService.countCartCostWithBonuses(0, 400));
        assertEquals(0, bonusService.countCartCostWithBonuses(0, 0));
    }

    @Test
    void countBonusAmountForCart_Negative() {
        assertThrows(IllegalArgumentException.class, () -> bonusService.countCartCostWithBonuses(-100, 400));
        assertThrows(IllegalArgumentException.class, () -> bonusService.countCartCostWithBonuses(-100, -400));
        assertThrows(IllegalArgumentException.class, () -> bonusService.countCartCostWithBonuses(100, -400));
    }

    private void assertCreatedBonusAccountIsCorrect(BonusAccount account) {
        assertNotNull(account);
        assertEquals(REG_PROMO_CODE_LENGTH, account.getRegistrationPromoCode().length());
        assertEquals(0, account.getBonusAmount());
        assertEquals(0, account.getBonusesSpent());
        assertFalse(account.isRegistrationPromoCodeUsed());
        assertFalse(account.isRegistrationPromoCashbackReturned());
    }

    private static final long CODE_OWNER_ID = 2L;

    private static User user() {
        User user = new User();
        user.setId(1L);
        user.setCountryCode("7");
        user.setMobileNumber("7777777777");
        user.setBonusAccount(bonusAccount());
        user.setUsedPromoCodes(new ArrayList<>());
        return user;
    }

    private static BonusAccount bonusAccount() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(0);
        account.setBonusesSpent(0);
        account.setRegistrationPromoCode("AAAAA");
        account.setRegistrationPromoCodeUsed(false);
        account.setRegistrationPromoCashbackReturned(false);
        return account;
    }

    private static User codeOwner() {
        User owner = new User();
        owner.setId(CODE_OWNER_ID);
        owner.setCountryCode("7");
        owner.setMobileNumber("8888888888");
        owner.setBonusAccount(ownerBonusAccount());
        owner.setUsedPromoCodes(new ArrayList<>());
        return owner;
    }

    private static BonusAccount ownerBonusAccount() {
        BonusAccount account = new BonusAccount();
        account.setBonusAmount(300);
        account.setBonusesSpent(0);
        account.setRegistrationPromoCode("BBBBB");
        account.setRegistrationPromoCodeUsed(false);
        account.setRegistrationPromoCashbackReturned(false);
        return account;
    }

    private static BonusAccount bonusAccountAfterApplyingRegPromoCode() {
        BonusAccount account = new BonusAccount();
        account.setRegistrationPromoCodeOwnerUserId(CODE_OWNER_ID);
        account.setBonusAmount(REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
        account.setBonusesSpent(0);
        account.setRegistrationPromoCode("AAAAA");
        account.setRegistrationPromoCodeUsed(true);
        account.setRegistrationPromoCashbackReturned(false);
        return account;
    }

    private static PromoCodeImpersonal codeImpersonal() {
        PromoCodeImpersonal code = new PromoCodeImpersonal();
        code.setId(1L);
        code.setAmount(2000);
        code.setPromoCode("IMP");
        return code;
    }


}